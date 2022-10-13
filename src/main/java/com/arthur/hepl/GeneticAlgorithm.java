package com.arthur.hepl;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GeneticAlgorithm implements Runnable
{
    private double uniformRate = 0.5;
    private double mutationRate = 0.025;
    private int keepCount = 0;
    private byte[] solution;
    private int populationSize = 50;
    private int maxIterations = 100;
    private Selection selectionMethod = new TournamentSelection(5, this);

    public GeneticAlgorithm(double uniformRate, double mutationRate, int keepCount)
    {
        this.uniformRate = uniformRate;
        this.mutationRate = mutationRate;
        this.keepCount = keepCount;
    }

    public GeneticAlgorithm()
    {

    }

    @Override
    public void run()
    {
        if (solution != null)
        {
            Population population = new Population(getPopulationSize(), getGeneSize());
            int i;
            for (i = 0; i < getMaxIterations() && population.getFittestValue(getSolution()) < getGeneSize(); i++)
            {
                population = evolve(population);
                /*for(int ind  = 0; ind < population.getIndividuals().size(); ind++)
                    DataStorage.store(i + 1, ind + 1, population.getIndividuals().get(ind).getFitness(solution));*/
                System.out.println("Generation: " + i + " Correct genes found: " + population.getFittestValue(getSolution()));
            }
            System.out.println("Solution found!");
            System.out.println("Generation: " + i);
            System.out.println("Genes: ");
            System.out.println(population.getFittest(getSolution()));
        } else
        {
            throw new RuntimeException("you must provide a solution...");
        }
    }

    public Population evolve(Population population)
    {
        Population temp = new Population();
        List<Individual> sorted = population.getIndividuals()
                .stream()
                .sorted(new IndividualsFitnessComparator(getSolution()))
                .limit(getKeepCount())
                .collect(Collectors.toList());
        temp.getIndividuals().addAll(sorted);
        for (int i = getKeepCount(); i < populationSize; i++)
        {
            Individual individual1 = selectionMethod.select(population);
            Individual individual2 = selectionMethod.select(population);
            Individual individual = crossover(individual1, individual2);
            mutate(individual);
            temp.getIndividuals().add(individual);
        }
        return temp;
    }

    private Individual crossover(Individual individual1, Individual individual2)
    {
        int geneSize = getGeneSize();
        Individual individual = new Individual(geneSize);
        for (int i = 0; i < geneSize; i++)
        {
            individual.setGene(i, Math.random() <= uniformRate ? individual1.getGene(i) : individual2.getGene(i));
        }
        return individual;
    }

    private void mutate(Individual individual)
    {
        for (int i = 0; i < getGeneSize(); i++)
        {
            if (Math.random() <= mutationRate)
                individual.setGene(i, (byte) Math.round(Math.random()));
        }
    }

    public double getUniformRate()
    {
        return uniformRate;
    }

    public void setUniformRate(double uniformRate)
    {
        this.uniformRate = uniformRate;
    }

    public double getMutationRate()
    {
        return mutationRate;
    }

    public void setMutationRate(double mutationRate)
    {
        this.mutationRate = mutationRate;
    }

    public int getKeepCount()
    {
        return keepCount;
    }

    public void setKeepCount(int keepCount)
    {
        this.keepCount = keepCount;
    }

    public byte[] getSolution()
    {
        return solution;
    }

    public void setSolution(String solution)
    {
        this.solution = solution.getBytes(StandardCharsets.UTF_8);
        for(int i = 0; i < this.solution.length; i++)
        {
            this.solution[i] -= 48;
        }
    }

    public int getPopulationSize()
    {
        return populationSize;
    }

    public void setPopulationSize(int populationSize)
    {
        this.populationSize = populationSize;
    }

    public int getMaxIterations()
    {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations)
    {
        this.maxIterations = maxIterations;
    }

    public int getGeneSize()
    {
        return solution.length;
    }

    public Selection getSelectionMethod()
    {
        return selectionMethod;
    }

    public void setSelectionMethod(Selection selectionMethod)
    {
        this.selectionMethod = selectionMethod;
    }
}
