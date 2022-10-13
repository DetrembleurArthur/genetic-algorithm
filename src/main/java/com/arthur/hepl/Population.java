package com.arthur.hepl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Population
{
    private final List<Individual> individuals = new ArrayList<>();

    public Population()
    {

    }

    public Population(int initialSize, int geneSize)
    {
        addPopulation(initialSize, geneSize);
    }

    public void addPopulation(int size, int geneSize)
    {
        for (int i = 0; i < size; i++)
        {
            Individual individual = new Individual(geneSize);
            individual.randomizeGenes();
            individuals.add(individual);
        }
    }

    public Individual getFittest(final byte[] solution)
    {
        Optional<Individual> optional = individuals.stream().max(new IndividualsFitnessComparator(solution));
        if (optional.isPresent())
            return optional.get();
        throw new RuntimeException("error in the individuals list (maximum fittest does not exist)");
    }

    public int getFittestValue(final byte[] solution)
    {
        Cache<Integer> cache = new Cache<>(Integer.MIN_VALUE);
        Optional<Individual> optional = individuals.stream().max(new IndividualsCacheFitnessComparator(solution, cache));
        if (optional.isPresent())
            return cache.getValue();
        throw new RuntimeException("error in the individuals list (maximum fittest does not exist)");
    }

    public List<Individual> getIndividuals()
    {
        return individuals;
    }
}
