package com.arthur.hepl.generic;

import java.util.ArrayList;
import java.util.Arrays;

public class RouletteSelection<T, R extends Comparable<R>, S> implements Selection<T>
{
    private final GeneticAlgorithm<T, R, S> algorithm;
    private final FitnessToDouble<R> fitnessToDouble;

    public RouletteSelection(GeneticAlgorithm<T, R, S> algorithm, FitnessToDouble<R> fitnessToDouble)
    {
        this.algorithm = algorithm;
        this.fitnessToDouble = fitnessToDouble;
    }

    @Override
    public Genome<T> select(Population<T> population)
    {
        final ArrayList<Double> fitnessCache = new ArrayList<>();
        double totalFitness = population.getGenomes()
                .stream()
                .mapToDouble(genome -> {
                    R fitness = algorithm.getFitnessCalculator().calculateFitness(genome, algorithm.getSolution());
                    double doubleFitness = fitnessToDouble.map(fitness);
                    fitnessCache.add(doubleFitness);
                    return doubleFitness;
                })
                .sum();
        double[] probabilities = fitnessCache
                .stream()
                .mapToDouble(fitness -> fitness / totalFitness)
                .toArray();
        double rand = Math.random() * Arrays.stream(probabilities).max().getAsDouble();
        double delta = Double.MAX_VALUE;
        int selectedIndex = 0;
        for(int i = 0; i < probabilities.length; i++)
        {
            if (probabilities[i] >= rand)
            {
                if (probabilities[i] - rand < delta)
                {
                    delta = probabilities[i] - rand;
                    selectedIndex = i;
                }
            }
        }
        return population.getGenomes().get(selectedIndex);
    }
}
