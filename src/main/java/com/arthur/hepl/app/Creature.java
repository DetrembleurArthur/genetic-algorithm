package com.arthur.hepl.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Creature
{
    private final ArrayList<Movements> movements = new ArrayList<>();
    private final Queue<Movements> movementsQueue = new ConcurrentLinkedQueue<>();
    private Movements current;
    private int movementIndex = 0;

    public void addMovement(Movements movement)
    {
        movements.add(movement);
    }

    public ArrayList<Movements> getMovements()
    {
        return movements;
    }

    public Queue<Movements> getMovementsQueue()
    {
        return movementsQueue;
    }

    public void addMovements(Movements ... movements)
    {
        this.movements.addAll(List.of(movements));
    }

    public void prepareQueue()
    {
        movementsQueue.clear();
        movementsQueue.addAll(movements);
    }

    public Movements nextMove()
    {
        if(movementsQueue.isEmpty())
            return null;
        current = movementsQueue.remove();
        int j = 0;
        for(Movements movement : movements)
        {
            if(movementIndex == j)
            {
                System.out.print("=> \033[92m" + movement + "\033[0m ");
            }
            else
            {
                System.out.print("=> " + movement + " ");
            }
            j++;
        }
        movementIndex++;
        System.out.println("\nMovement: " + current);
        return current;
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

    public boolean inMove()
    {
        return current != null;
    }

    public void noMove()
    {
        current = null;
    }

    public Movements getCurrent()
    {
        return current;
    }
}
