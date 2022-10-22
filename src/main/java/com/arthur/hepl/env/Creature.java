package com.arthur.hepl.env;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class Creature
{
    private final ArrayList<Movements> movements = new ArrayList<>();
    private final Queue<Movements> movementsQueue = new ArrayDeque<>();

    public Creature()
    {
    }

    public void createMovementsFromString(String movementsString)
    {
        movements.clear();
        movementsQueue.clear();
        for (char c : movementsString.toCharArray())
        {
            addMovement(Movements.values()[Integer.parseInt(String.valueOf(c))]);
        }
    }

    public void addMovement(Movements move)
    {
        movements.add(move);
    }

    public void prepareQueue()
    {
        movementsQueue.clear();
        movementsQueue.addAll(movements);
    }

    public Movements nextMovement()
    {
        return movementsQueue.poll();
    }

    public int remain()
    {
        return movementsQueue.size();
    }

    public int alreadyUsed()
    {
        return movements.size() - remain();
    }
}
