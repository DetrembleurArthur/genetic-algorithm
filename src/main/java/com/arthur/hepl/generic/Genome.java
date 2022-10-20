package com.arthur.hepl.generic;

import java.util.ArrayList;

public class Genome<T>
{
    private final ArrayList<T> genes = new ArrayList<>();


    public void randomize(int n, GeneRandomizer<T> randomizer)
    {
        genes.clear();
        for(int i = 0; i < n; i++)
        {
            T gene = randomizer.randomize();
            genes.add(gene);
        }
    }

    public ArrayList<T> getGenes()
    {
        return genes;
    }
}
