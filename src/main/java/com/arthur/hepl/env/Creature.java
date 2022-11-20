package com.arthur.hepl.env;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class Creature
{
    private final ArrayList<Movements> movements;
    private final Queue<Movements> movementsQueue = new ArrayDeque<>();

    public static Creature fromMoveString(String movementsString)
    {
        Creature creature = new Creature();
        for (char c : movementsString.toCharArray())
        {
            creature.movements.add(Movements.values()[Integer.parseInt(String.valueOf(c))]);
        }
        return creature;
    }

    public Creature(ArrayList<Movements> movements)
    {
        this.movements = movements;
    }

    public Creature()
    {
        this.movements = new ArrayList<>();
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
