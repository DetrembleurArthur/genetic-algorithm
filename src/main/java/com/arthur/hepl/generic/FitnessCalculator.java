package com.arthur.hepl.generic;

public interface FitnessCalculator<R, S>
{
    R calculateFitness(Genome<?> genome, S solution);
}
