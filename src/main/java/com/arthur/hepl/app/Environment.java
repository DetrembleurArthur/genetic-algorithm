package com.arthur.hepl.app;

import lombok.SneakyThrows;
import org.joml.Vector2i;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

public class Environment implements Runnable
{
    private static final HashMap<Integer, String> envCodes = new HashMap<>();

    static
    {
        envCodes.put(Cases.AIR, " ");
        envCodes.put(Cases.WALL, "\033[91m#\033[0m"); //mur
        envCodes.put(Cases.END, "\033[94;7mA\033[0m"); //arrivée
        envCodes.put(Cases.START, "\033[95;7mD\033[0m"); //debut
        envCodes.put(Cases.CREATURE, "\033[96;7mC\033[0m");
    }

    private final int[][] grid;
    private int cacheCase = Cases.START;
    private final int maxTickCount;
    private final Creature creature;
    private final Vector2i creaturePos = new Vector2i();
    private int tickMs = 0;

    public Environment(int width, int height, String envfilename, int maxTickCount, Creature creature)
    {
        this.grid = new int[height][width];
        this.maxTickCount = maxTickCount;
        this.creature = creature;
        if(!Objects.equals(envfilename, "random"))
            load_env(envfilename);
        else
            generateRandomGrid();
    }

    private void generateRandomGrid()
    {
        int height = grid.length;
        int width = grid[0].length;
        int startPosition = (int) ((width / 4) * Math.random());
        int endPosition = (int) ((width-1) - (width / 4) * Math.random());
        if(Math.random() <= 0.5)
        {
            int temp = startPosition;
            startPosition = endPosition;
            endPosition = temp;
        }
        int precHeight = (int) (height / 2 + Math.random() * (height / 4.0));
        for(int x = 0; x < width; x++)
        {
            int localHeight = precHeight;
            if(Math.random() < 0.8)
            {
               localHeight += (Math.random() >= 0.5 ? 1 : -1);
               if(localHeight < 0)
                   localHeight = 0;
               else if(localHeight >= height)
                   localHeight = height - 1;
            }
            for(int y = height - 1; y > height - localHeight; y--)
            {
                grid[y][x] = Cases.WALL;
            }
            if(startPosition == x)
            {
                grid[height - localHeight][startPosition] = Cases.START;
                creaturePos.set(startPosition, height - localHeight);
            }
            else if(endPosition == x)
            {
                grid[height - localHeight][endPosition] = Cases.END;
            }
            precHeight = localHeight;
        }
        showGrid();
        System.out.println("Enter movements");
        for(int i = 0; i < Movements.values().length; i++)
        {
            System.out.print("[" + i + " = " + Movements.values()[i] + "] ");
        }
        System.out.println("> ");
        Scanner scanner = new Scanner(System.in);
        String buffer = scanner.next();
        creature.createMovementsFromString(buffer);
    }

    public int getTickMs()
    {
        return tickMs;
    }

    public void setTickMs(int tickMs)
    {
        this.tickMs = tickMs;
    }

    private void load_env(String filename)
    {
        File file = new File(filename);
        if (file.exists())
        {
            try
            {
                Scanner scanner = new Scanner(new FileInputStream(file));
                int i = 0;
                while (scanner.hasNextInt())
                {
                    int value = scanner.nextInt();
                    int l = i / grid[0].length;
                    int c = i - grid[0].length * (i / grid[0].length);
                    grid[l][c] = value;
                    if (value == Cases.START)
                    {
                        creaturePos.set(c, l);
                    }
                    i++;
                }
            } catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void showGrid()
    {
        System.out.print("\033[93m+");
        for (int i = 0; i < grid[0].length; i++)
            System.out.print("--");
        System.out.println("+\033[0m");
        for (int[] row : grid)
        {
            System.out.print("\033[93m+\033[0m");
            for (int c : row)
            {
                System.out.print(envCodes.get(c) + " ");
            }
            System.out.println("\033[93m+\033[0m");
        }
        System.out.print("\033[93m+");
        for (int i = 0; i < grid[0].length; i++)
            System.out.print("--");
        System.out.println("+\033[0m");
    }

    private boolean outOfBand(Vector2i position)
    {
        return position.x < 0 || position.y < 0 || position.x >= grid[0].length || position.y >= grid.length;
    }

    private boolean collision(Vector2i position)
    {

        return outOfBand(position) || grid[position.y][position.x] == Cases.WALL;
    }

    private boolean arrived()
    {
        return cacheCase == Cases.END;
    }

    private boolean inAir()
    {
        Vector2i position = new Vector2i(creaturePos).sub(0, -1);
        if (!outOfBand(position))
        {
            return grid[position.y][position.x] == Cases.AIR || grid[position.y][position.x] == Cases.START;
        }
        return false;
    }

    private void processMove(Vector2i movementVector)
    {
        Vector2i postPosition = new Vector2i(creaturePos).add(movementVector);
        if (!collision(postPosition))
        {
            applyMove(postPosition);
            if (!inAir())
            {
                creature.noMove();
            }
        } else
        {
            creature.noMove();
        }
    }

    private void applyMove(Vector2i postPosition)
    {
        grid[creaturePos.y][creaturePos.x] = cacheCase;
        cacheCase = grid[postPosition.y][postPosition.x];
        creaturePos.set(postPosition);
        grid[creaturePos.y][creaturePos.x] = Cases.CREATURE;
    }

    @SneakyThrows
    @Override
    public void run()
    {
        Scanner scanner = new Scanner(System.in);
        creature.prepareQueue();
        grid[creaturePos.y][creaturePos.x] = Cases.CREATURE;
        for (int i = 0; i < maxTickCount; i++)
        {
            System.out.println("\033[0;0HT: " + (i + 1) + "/" + maxTickCount);
            Movements movements;
            if (!inAir())
            {
                if (!creature.inMove())
                {
                    movements = creature.nextMove();
                    if (movements == null)
                        return;

                } else
                {
                    movements = creature.getCurrent();
                }
                processMove(movements.getMoveVector());
            } else
            {
                Vector2i moveVector = new Vector2i(
                        creature.inMove() ? creature.getCurrent().getMoveVector().x : 0,
                        1);
                processMove(moveVector);
            }

            showGrid();
            if (arrived())
            {
                System.out.println("Arrived!");
                break;
            }
            if (tickMs == 0)
            {
                System.out.print("> ");
                scanner.nextLine();
            } else
            {
                Thread.sleep(tickMs);
            }
        }
    }

    public static Environment buildFromArgs(String[] args)
    {
        int width = Integer.parseInt(args[0]);
        int height = Integer.parseInt(args[1]);
        String filename = args[2];
        int maxTickCount = Integer.parseInt(args[3]);
        String moves = args[4];
        Creature creature = new Creature();
        creature.createMovementsFromString(moves);
        return new Environment(width, height, filename, maxTickCount, creature);
    }
}
