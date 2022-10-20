package com.arthur.hepl.generic;

public interface Selection<T>
{
    Genome<T> select(Population<T> population);   
}
