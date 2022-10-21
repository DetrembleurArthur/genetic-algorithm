package com.arthur.hepl.generic;

import java.util.Random;

public class Main
{
    //but: avoir une somme de chiffres vallant 1000
    public static void main(String[] args)
    {
        Random random = new Random();
        GeneticAlgorithm<Integer, Integer, Integer> algorithm = new GeneticAlgorithm<>();
        GeneRandomizer<Integer> randomizer = () -> random.nextInt(10000);
        TournamentSelection<Integer> tournamentSelection = new TournamentSelection<>(5, algorithm);
        FitnessCalculator<Integer, Integer> fitnessCalculator = (genome, solution) -> {
            int sum = genome.getGenes().stream()
                .mapToInt((g) -> (Integer)g)
                .sum();
            int solutionFitness = algorithm.getSolutionFitness();
            return solutionFitness - Math.abs(solutionFitness - sum);
        };
        algorithm.setBestKeepNumber(5);
        algorithm.setCrossoverRate(0.5);
        algorithm.setMutationRate(0.05);
        algorithm.setGenomeSize(5);
        algorithm.setMaxIterations(10000);
        algorithm.setPopulationSize(25);
        algorithm.setSolution(1000);
        algorithm.setSolutionFitness(1000);
        algorithm.setFitnessCalculator(fitnessCalculator);
        algorithm.setRandomizer(randomizer);
        algorithm.setSelection(tournamentSelection);

        algorithm.run();

        Genome<Integer> genome = algorithm.getFinalGenome();
        int sum = 0;
        for(Integer number : genome.getGenes())
        {
            System.out.println(number);
            sum += number;
        }
        System.out.println("sum: " + sum);
    }
}
