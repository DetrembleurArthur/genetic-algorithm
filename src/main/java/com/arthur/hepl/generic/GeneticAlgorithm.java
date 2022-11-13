package com.arthur.hepl.generic;

import lombok.SneakyThrows;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
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
    private StopGeneticCriteria<R> stopGeneticCriteria = new StopGeneticCriteria.StrictStopGeneticCriteria<>();
    private final Comparator<FitnessCache<T, R>> fitnessComparator = Comparator.comparing(o -> {
        try
        {
            return o.getFitnessFuture().get();
        } catch (InterruptedException | ExecutionException e)
        {
            return null;
        }
    });
    private Genome<T> finalGenome;

    private ArrayList<FitnessCache<T, R>> fitnessCache = new ArrayList<>();
    private ArrayList<FitnessCache<T, R>> fitnessCacheBackup = new ArrayList<>();
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

    public void stopThreadPool()
    {
        executor.shutdown();
    }

    private void computeFitness(Population<T> population)
    {
        for(Genome<T> genome : population.getGenomes())
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
        Instant start = Instant.now();
        Population<T> population = new Population<>();
        population.randomize(populationSize, genomeSize, randomizer);
        computeFitness(population);
        R fittest = null;
        int iteration = 0;
        while(iteration < maxIterations && !stopGeneticCriteria.mustBeStopped(fittest = getFittest().getFitnessFuture().get(), solutionFitness))
        {
            evolve();
            iteration++;
        }
        Instant end = Instant.now();
        System.out.println("Iterations: " + iteration);
        System.out.println("Fittest: " + fittest);
        System.out.println("Time: " + (end.toEpochMilli() - start.toEpochMilli()) + "ms");
        finalGenome = getFittest().getGenome();
    }
    
    public void evolve()
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
        for(int i = 0; i < genomeSize; i++)
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
        for(int i = 0; i < genomeSize; i++)
        {
            if(Math.random() <= mutationRate)
            {
                genome.getGenes().set(i, randomizer.randomize());
            }
        }
    }

    public FitnessCache<T, R> getFittest()
    {
        return fitnessCache.stream()
                .max(fitnessComparator).orElse(null);
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public int getGenomeSize() {
        return genomeSize;
    }

    public void setGenomeSize(int genomeSize) {
        this.genomeSize = genomeSize;
    }

    public GeneRandomizer<T> getRandomizer() {
        return randomizer;
    }

    public void setRandomizer(GeneRandomizer<T> randomizer) {
        this.randomizer = randomizer;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public FitnessCalculator<R, S> getFitnessCalculator() {
        return fitnessCalculator;
    }

    public void setFitnessCalculator(FitnessCalculator<R, S> fitnessCalculator) {
        this.fitnessCalculator = fitnessCalculator;
    }

    public S getSolution() {
        return solution;
    }

    public void setSolution(S solution) {
        this.solution = solution;
    }

    public R getSolutionFitness() {
        return solutionFitness;
    }

    public void setSolutionFitness(R solutionFitness) {
        this.solutionFitness = solutionFitness;
    }

    public int getBestKeepNumber() {
        return bestKeepNumber;
    }

    public void setBestKeepNumber(int bestKeepNumber) {
        this.bestKeepNumber = bestKeepNumber;
    }

    public Selection<T> getSelection() {
        return selection;
    }

    public void setSelection(Selection<T> selection) {
        this.selection = selection;
    }

    public double getCrossoverRate() {
        return crossoverRate;
    }

    public void setCrossoverRate(double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    public Genome<T> getFinalGenome() {
        return finalGenome;
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
}
