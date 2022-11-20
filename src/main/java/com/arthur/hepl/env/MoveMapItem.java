package com.arthur.hepl.env;

import org.joml.Vector2i;

import java.util.ArrayList;


public class MoveMapItem
{
    private final Vector2i destination;
    private final int ticksUsed;
    private final ArrayList<Move> movementsChain;

    public MoveMapItem(Vector2i destination, int ticksUsed, ArrayList<Move> movementsChain)
    {
        this.destination = destination;
        this.ticksUsed = ticksUsed;
        this.movementsChain = movementsChain;
    }

    public Vector2i getDestination()
    {
        return new Vector2i(destination);
    }

    public ArrayList<Move> getMovementsChain()
    {
        return new ArrayList<>(movementsChain);
    }

    public ArrayList<Move> getMovementsChainRef()
    {
        return movementsChain;
    }

    public int getTicksUsed()
    {
        return ticksUsed;
    }


}
