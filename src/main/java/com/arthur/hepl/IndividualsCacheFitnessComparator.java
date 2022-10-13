package com.arthur.hepl;


import java.util.Comparator;

public class IndividualsCacheFitnessComparator extends IndividualsFitnessComparator
{
    private final Cache<Integer> cache;

    public IndividualsCacheFitnessComparator(byte[] solution, final Cache<Integer> cache)
    {
        super(solution);
        this.cache = cache;
    }


    @Override
    public int compare(Individual o1, Individual o2)
    {
        int f1 = o1.getFitness(solution);
        int f2 = o2.getFitness(solution);
        caching(f1, f2);
        return Integer.compare(f1, f2);
    }

    private void caching(int f1, int f2)
    {
        int max = Integer.max(f1, f2);
        if(cache.getValue() < max)
            cache.setValue(max);
    }

    public Cache<Integer> getCache()
    {
        return cache;
    }
}
