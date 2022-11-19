package com.arthur.hepl.generic;

import com.arthur.hepl.perf.Recorder;
import lombok.SneakyThrows;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * T : genes value type
 * R : fitness type after calculation
 * S : solution type
 */
public class GeneticAlgorithm<T, R extends Comparable<R>, S> implements Runnable
{
    private int populationSize;
    private int genomeSize;
    private GeneRandomizer<T> randomizer;
    private int maxIterations;
    private FitnessCalculator<R, S> fitnessCalculator;
    private S solution;
    private R solutionFitness;
    private int bestKeepNumber;
    private Selection<T> selection;
    private double crossoverRate;
    private double mutationRate;
    private boolean manuallyControlled = false;
    private final Scanner userInput = new Scanner(System.in);
    private StatusRunner<T> statusRunner;
    private StopGeneticCriteria<R> stopGeneticCriteria = new StopGeneticCriteria.StrictStopGeneticCriteria<>();
    private GeneticResult<T, R> geneticResult;
    private Recorder recorder;

    private final Comparator<FitnessCache<T, R>> fitnessComparator = (o1, o2) -> {
        try
        {
            return o2.getFitnessFuture().get().compareTo(o1.getFitnessFuture().get());
        } catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        return 0;
    };

    private ArrayList<FitnessCache<T, R>> fitnessCache = new ArrayList<>();
    private ArrayList<FitnessCache<T, R>> fitnessCacheBackup = new ArrayList<>();
    private ThreadPoolExecutor executor;

    private int userControl(int iteration, FitnessCache<T, R> bestFitness) throws ExecutionException, InterruptedException
    {
        while (true)
        {
            System.out.println("iteration: " + iteration);
            System.out.println("Enter a command then press ENTER");
            System.out.print("> ");
            String command = userInput.nextLine();
            String[] tokens = command.split(" ");
            if (tokens.length >= 1)
            {
                switch (tokens[0])
                {
                    case "n":
                        if (tokens.length >= 2)
                        {
                            return Integer.parseInt(tokens[1]);
                        }
                        return 1;

                    case "s":
                        System.out.println("Best fitness: " + bestFitness.getFitnessFuture().get());
                        if (statusRunner != null)
                            statusRunner.run(bestFitness.getGenome().clone());
                }
            }
        }
    }

    public void stopThreadPool()
    {
        executor.shutdown();
    }

    private void computeFitness(Population<T> population)
    {
        for (Genome<T> genome : population.getGenomes())
        {
            Future<R> fitnessFuture = executor.submit(() -> fitnessCalculator.calculateFitness(genome, solution));
            FitnessCache<T, R> cache = new FitnessCache<>(genome, fitnessFuture);
            fitnessCache.add(cache);
        }
    }

    @SneakyThrows
    @Override
    public void run()
    {
        System.out.println("En cours...");
        geneticResult = null;
        fitnessCache.clear();
        fitnessCacheBackup.clear();
        Instant start = Instant.now();
        Population<T> population = new Population<>();
        population.randomize(populationSize, genomeSize, randomizer);
        computeFitness(population);
        FitnessCache<T, R> bestFitness = null;
        int iteration = 0;
        int stepIterations = 0;
        while (iteration < maxIterations && !stopGeneticCriteria.mustBeStopped((bestFitness = getFittest()).getFitnessFuture().get(), solutionFitness))
        {
            if (recorder != null)
            {
                recordIteration(iteration, false);
            }
            if (manuallyControlled)
            {
                if (iteration >= stepIterations)
                    stepIterations += userControl(iteration, bestFitness);
            }
            evolve();
            iteration++;
        }
        Instant end = Instant.now();
        System.out.println("Iterations: " + iteration);
        assert bestFitness != null;
        System.out.println("Fittest: " + bestFitness.getFitnessFuture().get());
        var time = end.toEpochMilli() - start.toEpochMilli();
        System.out.println("Time: " + time + "ms");
        geneticResult = new GeneticResult<>(bestFitness.getFitnessFuture().get(), bestFitness.getGenome(), iteration, (int) time);
        if (recorder != null)
        {
            recordIteration(iteration, true);
        }
    }

    public void evolve() throws ExecutionException, InterruptedException
    {
        Population<T> temp = new Population<>();
        List<FitnessCache<T, R>> sorted = fitnessCache
                .stream()
                .sorted(fitnessComparator)
                .limit(bestKeepNumber)
                .collect(Collectors.toList());
        temp.getGenomes().addAll(sorted.stream().map(FitnessCache::getGenome).collect(Collectors.toList()));
        fitnessCacheBackup.addAll(sorted);
        for (int i = bestKeepNumber; i < populationSize; i++)
        {
            Genome<T> genome1 = selection.select();
            Genome<T> genome2 = selection.select();
            Genome<T> child = crossover(genome1, genome2);
            mutate(child);
            Future<R> fitnessFuture = executor.submit(() -> fitnessCalculator.calculateFitness(child, solution));
            FitnessCache<T, R> cache = new FitnessCache<>(child, fitnessFuture);
            fitnessCacheBackup.add(cache);
            temp.getGenomes().add(child);
        }
        fitnessCache = fitnessCacheBackup;
        fitnessCacheBackup = new ArrayList<>();
    }

    private Genome<T> crossover(Genome<T> parent1, Genome<T> parent2)
    {
        Genome<T> child = new Genome<>();
        int genomeSize = parent1.getGenes().size();
        ArrayList<T> genesParent1 = parent1.getGenes();
        ArrayList<T> genesParent2 = parent2.getGenes();
        for (int i = 0; i < genomeSize; i++)
        {
            child.getGenes()
                    .add(Math.random() <= crossoverRate ?
                            genesParent1.get(i) : genesParent2.get(i));
        }
        return child;
    }

    private void mutate(Genome<T> genome)
    {
        int genomeSize = genome.getGenes().size();
        for (int i = 0; i < genomeSize; i++)
        {
            if (Math.random() <= mutationRate)
            {
                genome.getGenes().set(i, randomizer.randomize());
            }
        }
    }

    @SneakyThrows
    private void recordIteration(int iteration, boolean save)
    {
        int i = 1;
        for (FitnessCache<T, R> cache : fitnessCache)
        {
            recorder.addRow(String.valueOf(iteration + 1), String.valueOf(i), cache.getFitnessFuture().get().toString());
            i++;
        }
        if (save)
            recorder.write();
    }

    public FitnessCache<T, R> getFittest()
    {
        return fitnessCache.stream()
                .min(fitnessComparator).orElse(null);
    }

    public int getPopulationSize()
    {
        return populationSize;
    }

    public void setPopulationSize(int populationSize)
    {
        this.populationSize = populationSize;
    }

    public int getGenomeSize()
    {
        return genomeSize;
    }

    public void setGenomeSize(int genomeSize)
    {
        this.genomeSize = genomeSize;
    }

    public GeneRandomizer<T> getRandomizer()
    {
        return randomizer;
    }

    public void setRandomizer(GeneRandomizer<T> randomizer)
    {
        this.randomizer = randomizer;
    }

    public int getMaxIterations()
    {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations)
    {
        this.maxIterations = maxIterations;
    }

    public FitnessCalculator<R, S> getFitnessCalculator()
    {
        return fitnessCalculator;
    }

    public void setFitnessCalculator(FitnessCalculator<R, S> fitnessCalculator)
    {
        this.fitnessCalculator = fitnessCalculator;
    }

    public S getSolution()
    {
        return solution;
    }

    public void setSolution(S solution)
    {
        this.solution = solution;
    }

    public R getSolutionFitness()
    {
        return solutionFitness;
    }

    public void setSolutionFitness(R solutionFitness)
    {
        this.solutionFitness = solutionFitness;
    }

    public int getBestKeepNumber()
    {
        return bestKeepNumber;
    }

    public void setBestKeepNumber(int bestKeepNumber)
    {
        this.bestKeepNumber = bestKeepNumber;
    }

    public Selection<T> getSelection()
    {
        return selection;
    }

    public void setSelection(Selection<T> selection)
    {
        this.selection = selection;
    }

    public double getCrossoverRate()
    {
        return crossoverRate;
    }

    public void setCrossoverRate(double crossoverRate)
    {
        this.crossoverRate = crossoverRate;
    }

    public double getMutationRate()
    {
        return mutationRate;
    }

    public void setMutationRate(double mutationRate)
    {
        this.mutationRate = mutationRate;
    }

    public StopGeneticCriteria<R> getStopGeneticCriteria()
    {
        return stopGeneticCriteria;
    }

    public void setStopGeneticCriteria(StopGeneticCriteria<R> stopGeneticCriteria)
    {
        this.stopGeneticCriteria = stopGeneticCriteria;
    }

    public Comparator<FitnessCache<T, R>> getFitnessComparator()
    {
        return fitnessComparator;
    }

    public ArrayList<FitnessCache<T, R>> getFitnessCache()
    {
        return fitnessCache;
    }

    public ArrayList<FitnessCache<T, R>> getFitnessCacheBackup()
    {
        return fitnessCacheBackup;
    }

    public ThreadPoolExecutor getExecutor()
    {
        return executor;
    }

    public boolean isManuallyControlled()
    {
        return manuallyControlled;
    }

    public void setManuallyControlled(boolean manuallyControlled)
    {
        this.manuallyControlled = manuallyControlled;
    }

    public StatusRunner<T> getStatusRunner()
    {
        return statusRunner;
    }

    public void setStatusRunner(StatusRunner<T> statusRunner)
    {
        this.statusRunner = statusRunner;
    }

    public GeneticResult<T, R> getGeneticResult()
    {
        return geneticResult;
    }

    public Recorder getRecorder()
    {
        return recorder;
    }

    public void setRecorder(Recorder recorder)
    {
        this.recorder = recorder;
        recorder.setColumns("iterations", "genome", "fitness");
    }

    public void initThreadPool(int size)
    {
        if (executor == null)
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(size);
        else
            executor.setCorePoolSize(size);
    }
}
