package com.arthur.hepl.generic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
    private final Comparator<Genome<T>> fitnessComparator = (g1, g2) -> {
        R fitness1 = fitnessCalculator.calculateFitness(g1, solution);
        R fitness2 = fitnessCalculator.calculateFitness(g2, solution);
        return fitness1.compareTo(fitness2);
    };
    private Genome<T> finalGenome;

    @Override
    public void run()
    {
        Population<T> population = new Population<>();
        population.randomize(populationSize, genomeSize, randomizer);
        int iteration = 0;
        while(iteration < maxIterations && getFittestValue(population).compareTo(solutionFitness) != 0)
        {
            population = evolve(population);
            iteration++;
        }
        System.out.println("Iterations: " + iteration);
        finalGenome = getFittest(population);
    }
    
    public Population<T> evolve(Population<T> population)
    {
        Population<T> temp = new Population<>();
        List<Genome<T>> sorted = population.getGenomes()
                .stream()
                .sorted(fitnessComparator)
                .limit(bestKeepNumber)
                .collect(Collectors.toList());
        temp.getGenomes().addAll(sorted);
        for (int i = bestKeepNumber; i < populationSize; i++)
        {
            Genome<T> genome1 = selection.select(population);
            Genome<T> genome2 = selection.select(population);
            Genome<T> child = crossover(genome1, genome2);
            mutate(child);
            temp.getGenomes().add(child);
        }
        return temp;
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

    public Genome<T> getFittest(Population<T> population)
    {
        Optional<Genome<T>> optional = population.getGenomes()
            .stream()
            .max(fitnessComparator);
        return optional.orElse(null);
    }

    public R getFittestValue(Population<T> population)
    {
        Optional<Genome<T>> optional = population.getGenomes()
            .stream()
            .max(fitnessComparator);
        if(optional.isPresent())
            return fitnessCalculator.calculateFitness(optional.get(), solution);
        return null;
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

    public Comparator<Genome<T>> getFitnessComparator() {
        return fitnessComparator;
    }

    public Genome<T> getFinalGenome() {
        return finalGenome;
    }
}
