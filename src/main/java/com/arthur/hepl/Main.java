package com.arthur.hepl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Main
{
    public static void main(String[] args)
    {
        GeneticAlgorithm algorithm = new GeneticAlgorithm();
        algorithm.setUniformRate(0.5);
        algorithm.setMutationRate(0.025);
        algorithm.setKeepCount(1);
        algorithm.setMaxIterations(2000);
        algorithm.setPopulationSize(25);
        algorithm.setSolution("0101010101010101010101010101010101010101010101010101010101010101");
        TournamentSelection selection = new TournamentSelection(5, algorithm);
        RouletteSelection selection1 = new RouletteSelection(algorithm);
        algorithm.setSelectionMethod(selection1);

        algorithm.run();
    }
}
