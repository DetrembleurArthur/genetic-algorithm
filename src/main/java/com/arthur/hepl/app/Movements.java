package com.arthur.hepl.app;

import org.joml.Vector2i;

public enum Movements
{
    DOWN(0, 1),
    DOWN_RIGHT(1, 1),
    RIGHT(1, 0),
    UP_RIGHT(1, -1),
    UP(0, -1),
    UP_LEFT(-1, -1),
    LEFT(-1, 0),
    DOWN_LEFT(-1, 1);

    private final Vector2i moveVector;

    Movements(int x, int y)
    {
        moveVector = new Vector2i(x, y);
    }

    public Vector2i getMoveVector()
    {
        return moveVector;
    }
}
