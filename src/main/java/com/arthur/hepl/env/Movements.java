package com.arthur.hepl.env;

import org.joml.Vector2i;

@Immutable
public enum Movements
{
    DOWN(0, 1),
    DOWN_RIGHT(1, 1),
    RIGHT(1, 0),
    UP_RIGHT(1, -1),
    UP(0, -1),
    UP_LEFT(-1, -1),
    LEFT(-1, 0),
    DOWN_LEFT(-1, 1),
    BLOCKED(0, 0);

    private final Vector2i moveVector;

    Movements(int x, int y)
    {
        moveVector = new Vector2i(x, y);
    }

    public Vector2i getMoveVector()
    {
        return new Vector2i(moveVector);
    }

    public static Movements fromVector(Vector2i vector)
    {
        for(Movements move : Movements.values())
        {
            if(move.getMoveVector().equals(vector))
            {
                return move;
            }
        }
        return null;
    }
}
