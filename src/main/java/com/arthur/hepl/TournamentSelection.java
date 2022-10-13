package com.arthur.hepl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TournamentSelection implements Selection
{
    private int tournamentSize;
    private final GeneticAlgorithm algorithm;

    public TournamentSelection(int tournamentSize, final GeneticAlgorithm algorithm)
    {
        this.tournamentSize = tournamentSize;
        this.algorithm = algorithm;
    }

    @Override
    public Individual select(Population population)
    {
        Population tournament = new Population();
        List<Individual> temp = population.getIndividuals()
                .stream()
                .sorted(Comparator.comparingDouble(o -> Math.random()))
                .limit(tournamentSize)
                .collect(Collectors.toList());
        tournament.getIndividuals().addAll(temp);
        return tournament.getFittest(algorithm.getSolution());
    }

    public int getTournamentSize()
    {
        return tournamentSize;
    }

    public void setTournamentSize(int tournamentSize)
    {
        this.tournamentSize = tournamentSize;
    }
}
