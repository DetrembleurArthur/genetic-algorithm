package com.arthur.hepl.generic;

public class GeneticResult<T, R>
{
    final private R fitness;
    final private Genome<T> genome;
    final private int iterations;
    final private int timeMs;

    public GeneticResult(R fitness, Genome<T> genome, int iterations, int timeMs)
    {
        this.fitness = fitness;
        this.genome = genome;
        this.iterations = iterations;
        this.timeMs = timeMs;
    }

    public R getFitness()
    {
        return fitness;
    }

    public Genome<T> getGenome()
    {
        return genome;
    }

    public int getIterations()
    {
        return iterations;
    }

    public int getTimeMs()
    {
        return timeMs;
    }
}
