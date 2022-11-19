package com.arthur.hepl.generic;

import java.util.Comparator;

public class TournamentSelection<T, R extends Comparable<R>> implements Selection<T>
{
    private final int tournamentSize;
    private final GeneticAlgorithm<T, R, ?> algorithm;

    public TournamentSelection(int tournamentSize, GeneticAlgorithm<T, R, ?> algorithm)
    {
        this.tournamentSize = tournamentSize;
        this.algorithm = algorithm;
    }

    @Override
    public Genome<T> select()
    {
        FitnessCache<T, R> fitnessCache = algorithm.getFitnessCache()
                .stream()
                //sert à randomiser le stream
                .sorted(Comparator.comparingDouble(o -> Math.random()))
                .limit(tournamentSize)
                //min car le fitness comparator sert à trier dans l'ordre décroissant!
                .min(algorithm.getFitnessComparator()).orElse(null);
        assert fitnessCache != null;
        return fitnessCache.getGenome();
    }
}
