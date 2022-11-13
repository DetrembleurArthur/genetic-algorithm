package com.arthur.hepl.generic;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

public class Population<T>
{
    private final ArrayList<Genome<T>> genomes = new ArrayList<>();

    public void randomize(int populationSize, int genomeSize, GeneRandomizer<T> randomizer)
    {
        for(int i = 0; i < populationSize; i++)
        {
            Genome<T> genome = new Genome<>();
            genome.randomize(genomeSize, randomizer);
            genomes.add(genome);
        }
    }

    public ArrayList<Genome<T>> getGenomes()
    {
        return genomes;
    }
}
