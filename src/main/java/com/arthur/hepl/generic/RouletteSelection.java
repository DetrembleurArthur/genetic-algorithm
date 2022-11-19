package com.arthur.hepl.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

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
    public Genome<T> select()
    {
        final ArrayList<Double> fitnessCache = new ArrayList<>();
        double totalFitness = algorithm.getFitnessCache()
                .stream()
                .mapToDouble(cache -> {
                    R fitness = null;
                    try
                    {
                        fitness = cache.getFitnessFuture().get();
                    } catch (InterruptedException | ExecutionException e)
                    {
                        e.printStackTrace();
                    }
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
        for (int i = 0; i < probabilities.length; i++)
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
        return algorithm.getFitnessCache().get(selectedIndex).getGenome();
    }
}
