package com.arthur.hepl.generic;

import java.util.Random;

public class Main
{
    //but: avoir une somme de chiffres vallant 1000
    public static void main(String[] args)
    {
        Random random = new Random();

        GeneticAlgorithm<Character, Integer, String> algorithm = new GeneticAlgorithm<>();

        GeneRandomizer<Character> randomizer = () -> random.nextBoolean() ? '0' : '1';

        TournamentSelection<Character> tournamentSelection = new TournamentSelection<>(5, algorithm);

        FitnessCalculator<Integer, String> fitnessCalculator = (genome, solution) -> {
            int sum = 0;
            for(int i = 0; i < solution.length(); i++)
            {
                if(solution.charAt(i) == (char)genome.getGenes().get(i))
                    sum++;
            }
            return sum;
        };

        String solution = "0101010101010101010101010101010101010101010101010101010101010101";

        algorithm.setBestKeepNumber(5);
        algorithm.setCrossoverRate(0.5);
        algorithm.setMutationRate(0.05);
        algorithm.setGenomeSize(solution.length());
        algorithm.setMaxIterations(10000);
        algorithm.setPopulationSize(25);
        algorithm.setSolution(solution);
        algorithm.setSolutionFitness(solution.length());
        algorithm.setFitnessCalculator(fitnessCalculator);
        algorithm.setRandomizer(randomizer);
        algorithm.setSelection(tournamentSelection);

        algorithm.run();

        Genome<Character> genome = algorithm.getFinalGenome();
        for(Character c : genome.getGenes())
        {
            System.out.print(c);
        }
        System.out.println();
    }
}
