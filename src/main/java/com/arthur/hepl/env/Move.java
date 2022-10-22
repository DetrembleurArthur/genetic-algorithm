package com.arthur.hepl.env;

public class Move
{
    private final Movements real;
    private final Movements origin;
    private final boolean environmental;

    public Move(Movements real, Movements origin, boolean environmental)
    {
        this.real = real;
        this.origin = origin == null ? real : origin;
        this.environmental = environmental;
    }

    public Movements getReal()
    {
        return real;
    }

    public Movements getOrigin()
    {
        return origin;
    }

    public boolean isEnvironmental()
    {
        return environmental;
    }
}
