package com.arthur.hepl.generic;

public interface FitnessToDouble<R>
{
    double map(R fitness);

    class IntegerFitnessToDouble implements FitnessToDouble<Integer>
    {

        @Override
        public double map(Integer fitness)
        {
            return (double) fitness;
        }
        
    }

    class DoubleFitnessToDouble implements FitnessToDouble<Double>
    {

        @Override
        public double map(Double fitness)
        {
            return fitness;
        }
        
    }
}
