package com.arthur.hepl;

import java.util.ArrayList;
import java.util.Arrays;

public class RouletteSelection implements Selection
{
    private final GeneticAlgorithm algorithm;

    public RouletteSelection(final GeneticAlgorithm algorithm)
    {
        this.algorithm = algorithm;
    }

    @Override
    public Individual select(Population population)
    {
        final ArrayList<Integer> fitnessCache = new ArrayList<>();
        int totalFitness = population.getIndividuals()
                .stream()
                .mapToInt(individual -> {
                    int fitness = individual.getFitness(algorithm.getSolution());
                    fitnessCache.add(fitness);
                    return fitness;
                })
                .sum();
        double[] probabilities = fitnessCache
                .stream()
                .mapToDouble(fitness -> fitness / (double) totalFitness)
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
        return population.getIndividuals().get(selectedIndex);
    }
}
