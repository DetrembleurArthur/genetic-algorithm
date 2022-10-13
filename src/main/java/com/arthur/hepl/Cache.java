package com.arthur.hepl;

public class Cache<T>
{
    private T value;

    public Cache(T value)
    {
        this.value = value;
    }

    public T getValue()
    {
        return value;
    }

    public void setValue(T value)
    {
        this.value = value;
    }
}
