package com.arthur.hepl;


import java.util.Comparator;

public class IndividualsFitnessComparator implements Comparator<Individual>
{
    protected final byte[] solution;

    public IndividualsFitnessComparator(final byte[] solution)
    {
        this.solution = solution;
    }

    @Override
    public int compare(Individual o1, Individual o2)
    {
        return Integer.compare(o1.getFitness(solution), o2.getFitness(solution));
    }
}
