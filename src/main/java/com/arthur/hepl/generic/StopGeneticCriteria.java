package com.arthur.hepl.generic;

public interface StopGeneticCriteria<R extends Comparable<R>>
{
    boolean mustBeStopped(R bestFitness, R solutionFitness);

    class StrictStopGeneticCriteria<R extends Comparable<R>> implements StopGeneticCriteria<R>
    {
        @Override
        public boolean mustBeStopped(R bestFitness, R solutionFitness)
        {
            return bestFitness.compareTo(solutionFitness) == 0;
        }
    }
}
