package com.arthur.hepl.generic;

import java.util.concurrent.Future;

public class FitnessCache<T, R>
{
    private final Genome<T> genome;
    private final Future<R> fitnessFuture;

    public FitnessCache(Genome<T> genome, Future<R> fitnessFuture)
    {
        this.genome = genome;
        this.fitnessFuture = fitnessFuture;
    }

    public Genome<T> getGenome()
    {
        return genome;
    }

    public Future<R> getFitnessFuture()
    {
        return fitnessFuture;
    }
}
