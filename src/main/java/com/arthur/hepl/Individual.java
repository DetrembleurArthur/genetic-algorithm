package com.arthur.hepl;

public class Individual
{
    public static final int DEFAULT_GENE_SIZE = 64;
    private final byte[] genes;

    public Individual()
    {
        this(DEFAULT_GENE_SIZE);
    }

    public Individual(int geneSize)
    {
        genes = new byte[geneSize];
    }

    public void randomizeGenes()
    {
        for(int i = 0; i < genes.length; i++)
        {
            genes[i] = (byte) Math.round(Math.random());
        }
    }

    public int getFitness(final byte[] solution)
    {
        int fitness = 0;
        if(genes.length == solution.length)
        {
            for(int i = 0; i < solution.length; i++)
            {
                if(genes[i] == solution[i])
                    fitness++;
            }
        }
        else
        {
            throw new RuntimeException("gene size and solution size mismatch: " + genes.length + " != " + solution.length);
        }
        return fitness;
    }

    public void setGene(int i, byte gene)
    {
        genes[i] = gene;
    }

    public byte getGene(int i)
    {
        return genes[i];
    }

    public byte[] getGenes()
    {
        return genes;
    }

    @Override
    public String toString()
    {
        StringBuilder geneString = new StringBuilder();
        for (byte gene : genes)
        {
            geneString.append(gene);
        }
        return geneString.toString();
    }
}
