package com.arthur.hepl.generic;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TournamentSelection<T> implements Selection<T>
{
    private int tournamentSize;
    private GeneticAlgorithm<T, ?, ?> algorithm;

    public TournamentSelection(int tournamentSize, GeneticAlgorithm<T, ?, ?> algorithm)
    {
        this.tournamentSize = tournamentSize;
        this.algorithm = algorithm;
    }

    @Override
    public Genome<T> select(Population<T> population)
    {
        Population<T> tournament = new Population<>();
        List<Genome<T>> temp = population.getGenomes()
                .stream()
                .sorted(Comparator.comparingDouble(o -> Math.random()))
                .limit(tournamentSize)
                .collect(Collectors.toList());
        tournament.getGenomes().addAll(temp);
        return algorithm.getFittest(tournament);
    }
}
