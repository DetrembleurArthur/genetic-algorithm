package com.arthur.hepl.env;

import org.joml.Vector2i;

@Immutable
public class EnvCalculationResult
{
    private final Vector2i finalPosition = new Vector2i();
    private final double distanceWithEndPosition;
    private final int tickCount;
    private final int movesUsed;

    public EnvCalculationResult(Vector2i finalPosition, double distanceWithEndPosition, int tickCount, int movesUsed)
    {
        this.finalPosition.set(finalPosition);
        this.tickCount = tickCount;
        this.distanceWithEndPosition = distanceWithEndPosition;
        this.movesUsed = movesUsed;
    }

    public Vector2i getFinalPosition()
    {
        return new Vector2i(finalPosition);
    }

    public int getTickCount()
    {
        return tickCount;
    }

    public double getDistanceWithEndPosition()
    {
        return distanceWithEndPosition;
    }

    public int getMovesUsed()
    {
        return movesUsed;
    }

    @Override
    public String toString()
    {
        return "EnvCalculationResult{" +
                "finalPosition=" + finalPosition +
                ", distanceWithEndPosition=" + distanceWithEndPosition +
                ", tickCount=" + tickCount +
                ", movesUsed=" + movesUsed +
                '}';
    }
}
