package com.arthur.hepl.env;

import lombok.SneakyThrows;
import org.joml.Vector2i;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

@Immutable
public class Environnement
{
    private static final HashMap<Byte, String> envCodes = new HashMap<>();

    enum EnvFileType
    {
        COORD_Y_TO_DOWN,
        COORD_Y_TO_UP,
        TILES
    }

    static
    {
        envCodes.put(Cases.AIR, " ");
        envCodes.put(Cases.WALL, "\033[91m#\033[0m"); //mur
        envCodes.put(Cases.END, "\033[94;7mA\033[0m"); //arrivée
        envCodes.put(Cases.START, "\033[95;7mD\033[0m"); //debut
        envCodes.put(Cases.CREATURE, "\033[96;7mC\033[0m");
    }

    private final int width;
    private final int height;
    private final byte[][] grid;
    private final Vector2i startPosition = new Vector2i();
    private final Vector2i endPosition = new Vector2i();
    private final int maxTickCount;
    private HashMap<Movements, MoveMapItem[][]> moveMap;

    public Environnement(int width, int height, int maxTickCount)
    {
        this.width = width;
        this.height = height;
        this.maxTickCount = maxTickCount;
        grid = new byte[height][width];
    }


    public static Environnement buildFromArgs(String[] args)
    {
        if (args.length >= 4)
        {
            int width = Integer.parseInt(args[0]);
            int height = Integer.parseInt(args[1]);
            String filename = args[2];
            int ticks = Integer.parseInt(args[3]);
            Environnement env = new Environnement(width, height, ticks);
            EnvFileType fileType = EnvFileType.TILES;
            if (args.length == 5)
                fileType = EnvFileType.valueOf(args[4]);
            if (filename.equals("random"))
                env.generateRandomGrid(fileType);
            else
                env.loadEnv(filename, fileType);
            env.computeMoveMap();
            return env;
        }
        return null;
    }

    private void loadTileFile(Scanner scanner)
    {
        int i = 0;
        while (scanner.hasNextInt())
        {
            int value = scanner.nextInt();
            int l = i / grid[0].length;
            int c = i - grid[0].length * (i / grid[0].length);
            grid[l][c] = (byte) value;
            if (value == Cases.START)
            {
                startPosition.set(c, l);
            } else if (value == Cases.END)
            {
                endPosition.set(c, l);
            }
            i++;
        }
    }

    private void loadCoordFile(Scanner scanner, EnvFileType fileType)
    {
        int i = 0;
        while (scanner.hasNextLine())
        {
            String[] line = scanner.nextLine().split(" ");
            int x = Integer.parseInt(line[0]);
            int y = Integer.parseInt(line[1]);
            if (fileType.equals(EnvFileType.COORD_Y_TO_UP))
                y = height - y - 1;
            byte value = Cases.WALL;
            if (i == 0)
            {
                value = Cases.START;
                startPosition.set(x, y);
            } else if (i == 1)
            {
                value = Cases.END;
                endPosition.set(x, y);
            }
            grid[y][x] = value;
            i++;
        }
    }

    public void loadEnv(String filename, EnvFileType fileType)
    {
        File file = new File(filename);
        if (file.exists())
        {
            try
            {
                Scanner scanner = new Scanner(new FileInputStream(file));
                if (fileType.equals(EnvFileType.TILES))
                {
                    loadTileFile(scanner);
                } else
                {
                    loadCoordFile(scanner, fileType);
                }

            } catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    //à améliorer...
    @SneakyThrows
    public void generateRandomGrid(EnvFileType fileType)
    {
        int startPosition = (int) ((width / 4) * Math.random());
        int endPosition = (int) ((width - 1) - (width / 4) * Math.random());
        if (Math.random() <= 0.5)
        {
            int temp = startPosition;
            startPosition = endPosition;
            endPosition = temp;
        }
        int precHeight = (int) (height / 2 + Math.random() * (height / 4.0));
        for (int x = 0; x < width; x++)
        {
            int localHeight = precHeight;
            if (Math.random() < 0.8)
            {
                localHeight += (Math.random() >= 0.5 ? 1 : -1);
                if (localHeight < 0)
                    localHeight = 0;
                else if (localHeight >= height)
                    localHeight = height - 1;
            }
            for (int y = height - 1; y > height - localHeight; y--)
            {
                grid[y][x] = Cases.WALL;
            }
            if (startPosition == x)
            {
                grid[height - localHeight][startPosition] = Cases.START;
                this.startPosition.set(startPosition, height - localHeight);
            } else if (endPosition == x)
            {
                grid[height - localHeight][endPosition] = Cases.END;
                this.endPosition.set(endPosition, height - localHeight);
            }
            precHeight = localHeight;
        }
        File file = new File("random-env.txt");
        FileWriter writer = new FileWriter(file);
        for (int i = 0; i < height; i++)
        {
            for (int j = 0; j < width; j++)
            {
                writer.write(grid[i][j] + " ");
            }
            writer.write("\n");
        }
        writer.flush();
    }

    public boolean outOfBand(Vector2i position)
    {
        return position.x < 0 || position.x >= width || position.y < 0 || position.y >= height;
    }

    public synchronized byte getCase(Vector2i position)
    {
        if (!outOfBand(position))
            return grid[position.y][position.x];
        return Cases.OOB;
    }

    public boolean inAir(Vector2i position)
    {
        int gridCase = getCase(new Vector2i(position).add(0, 1));
        return gridCase == Cases.AIR || gridCase == Cases.END || gridCase == Cases.START;
    }

    public boolean inWall(Vector2i position)
    {
        int gridCase = getCase(position);
        return gridCase == Cases.WALL || gridCase == Cases.OOB;
    }

    public void computeMoveMap()
    {
        moveMap = new HashMap<>();
        for (Movements move : Movements.values())
        {
            if (move.equals(Movements.BLOCKED))
                continue;
            MoveMapItem[][] items = new MoveMapItem[height][width];
            moveMap.put(move, items);
            //System.out.println("MAP: " + move);
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    Vector2i destination = new Vector2i(x, y);
                    int ticksUsed = 1;
                    ArrayList<Move> movementsChain = new ArrayList<>();
                    if (grid[y][x] == Cases.AIR || grid[y][x] == Cases.START)
                    {
                        if (inWall(new Vector2i(x, y + 1)))
                        {
                            ticksUsed += move(destination, move, movementsChain, 0) - 1;
                        }
                    }
                    items[y][x] = new MoveMapItem(destination, ticksUsed, movementsChain);
                    //System.out.print(destination.x + ":" + destination.y + ":" + ticksUsed + "| ");
                }
                //System.out.println();
            }
        }
    }

    public int move(Vector2i position, Movements movement, ArrayList<Move> movementsChain, int currentTick)
    {
        int tickPassed = 0;
        Vector2i move = movement.getMoveVector();
        Vector2i temp = new Vector2i(position);
        temp.add(move);
        if (!inWall(temp))
        {
            if (movementsChain != null)
                movementsChain.add(new Move(movement, null, false));
            while (currentTick + tickPassed < maxTickCount && inAir(temp))
            {
                if (inWall(new Vector2i(temp).add(move.x, 1)))
                {
                    move.x = 0;
                }
                temp.add(move.x, 1);
                if (movementsChain != null)
                    movementsChain.add(new Move(Movements.fromVector(new Vector2i(move.x, 1)), null, true));
                tickPassed++;
            }
            position.set(temp);
        } else
        {
            if (movementsChain != null)
                movementsChain.add(new Move(Movements.BLOCKED, movement, false));
        }
        tickPassed++;
        return tickPassed;
    }

    public boolean arrived(Vector2i position)
    {
        return getCase(position) == Cases.END;
    }


    public EnvCalculationResult calculateFinalPositionWithMap(Creature creature, ArrayList<Move> movementsChain)
    {
        boolean internalMc = movementsChain == null;
        if (internalMc)
            movementsChain = new ArrayList<>();
        creature.prepareQueue();
        Vector2i finalPosition;
        Vector2i officialEndPosition;
        int tickCountLimit;
        synchronized (this)
        {
            finalPosition = new Vector2i(startPosition);
            officialEndPosition = new Vector2i(endPosition);
            tickCountLimit = maxTickCount;
        }
        int i = 0, tickOutBand;
        Movements move;
        MoveMapItem item;
        while (i < tickCountLimit && !arrived(finalPosition) && (move = creature.nextMovement()) != null)
        {
            synchronized (this)
            {
                item = moveMap.get(move)[finalPosition.y][finalPosition.x];
            }
            i += item.getTicksUsed();
            finalPosition = item.getDestination();
            if(internalMc)
                movementsChain = item.getMovementsChainRef();
            else
                movementsChain.addAll(item.getMovementsChain());
        }
        if (i > tickCountLimit)
        {
            tickOutBand = i % tickCountLimit;
            i -= tickOutBand;
            System.out.println(tickOutBand + " " + i);
            for (int j = movementsChain.size() - 1; j >= movementsChain.size() - tickOutBand; j--)
            {
                finalPosition.sub(movementsChain.get(j).getReal().getMoveVector());
            }
        }
        double distance = finalPosition.distance(officialEndPosition);
        return new EnvCalculationResult(finalPosition, distance, i, creature.alreadyUsed());
    }

    public EnvCalculationResult calculateFinalPosition(Creature creature, ArrayList<Move> movementsChain)
    {
        if (movementsChain == null)
            movementsChain = new ArrayList<>();
        creature.prepareQueue();
        Vector2i finalPosition;
        Vector2i officialEndPosition;
        int tickCountLimit;
        synchronized (this)
        {
            finalPosition = new Vector2i(startPosition);
            officialEndPosition = new Vector2i(endPosition);
            tickCountLimit = maxTickCount;
        }
        Movements move;
        int i = 0, tickOutBand;
        while (i < tickCountLimit && !arrived(finalPosition) && (move = creature.nextMovement()) != null)
        {
            i += move(finalPosition, move, movementsChain, i);
        }
        if (i > tickCountLimit)
        {
            tickOutBand = i % tickCountLimit;
            i -= tickOutBand;
            System.out.println(tickOutBand + " " + i);
            for (int j = movementsChain.size() - 1; j >= movementsChain.size() - tickOutBand; j--)
            {
                finalPosition.sub(movementsChain.get(j).getReal().getMoveVector());
            }
        }
        double distance = finalPosition.distance(officialEndPosition);
        //System.out.println("ticks: " + i + " / " + maxTickCount);
        return new EnvCalculationResult(finalPosition, distance, i, creature.alreadyUsed());
    }

    public void showGrid(Vector2i creaturePosition)
    {
        System.out.print("\033[93m+ ");
        for (int i = 0; i < grid[0].length; i++)
            System.out.print("- ");
        System.out.println("+\033[0m");
        for (int y = 0; y < height; y++)
        {
            System.out.print("\033[93m+\033[0m ");
            for (int x = 0; x < width; x++)
            {
                byte gridCase = grid[y][x];
                if (creaturePosition != null && creaturePosition.equals(x, y))
                {
                    System.out.print(envCodes.get(Cases.CREATURE) + " ");
                } else
                {
                    System.out.print(envCodes.get(gridCase) + " ");
                }
            }
            System.out.println("\033[93m+\033[0m");
        }
        System.out.print("\033[93m+ ");
        for (int i = 0; i < grid[0].length; i++)
            System.out.print("- ");
        System.out.println("+\033[0m");
    }

    public void animate(Creature creature, int tickMs, boolean realtimeComputation)
    {
        ArrayList<Move> movementsChain = new ArrayList<>();
        EnvCalculationResult result = realtimeComputation ?
                calculateFinalPosition(creature, movementsChain) :
                calculateFinalPositionWithMap(creature, movementsChain);
        Vector2i position = new Vector2i(startPosition);
        int tick = 0;
        int i = 0;
        int len = movementsChain.size();
        var list = len > maxTickCount ? movementsChain.subList(0, len - (len - maxTickCount)) : movementsChain;
        for (Move move : list)
        {
            System.out.println("\033[0;0H");
            int j = 0;
            for (Move sub : list)
            {
                if (i == j)
                    System.out.print(" -> \033[95m" + sub.getOrigin() + "\033[0m");
                else
                    System.out.print(" -> " + sub.getOrigin());
                j++;
            }
            tick++;
            System.out.println("\nTick : " + tick + " / " + maxTickCount);
            System.out.println("real: " + move.getReal() + " origin: " + move.getOrigin() + " " + (move.isEnvironmental() ? "\033[92menvironmental movement\033[0m" : "\033[93mcreature movement\033[0m"));
            position.add(move.getReal().getMoveVector());
            showGrid(position);
            i++;
            try
            {
                Thread.sleep(tickMs);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        System.out.println("\n" + result);
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public byte[][] getGrid()
    {
        return grid.clone();
    }

    public Vector2i getStartPosition()
    {
        return new Vector2i(startPosition);
    }

    public Vector2i getEndPosition()
    {
        return new Vector2i(endPosition);
    }


    //test de performances
    public static void main(String[] args)
    {
        Environnement environnement = Environnement.buildFromArgs(new String[]{
                "12", "6", "env-x-y.txt", "100", "COORD_Y_TO_DOWN"
        });
        environnement.computeMoveMap();
        ArrayList<Movements> movements = new ArrayList<>();
        movements.add(Movements.RIGHT);
        movements.add(Movements.UP_RIGHT);
        movements.add(Movements.UP_RIGHT);
        movements.add(Movements.UP_RIGHT);
        movements.add(Movements.UP_RIGHT);
        movements.add(Movements.UP_RIGHT);
        movements.add(Movements.UP_RIGHT);
        movements.add(Movements.UP_RIGHT);
        movements.add(Movements.UP_RIGHT);
        movements.add(Movements.UP_RIGHT);
        movements.add(Movements.UP_RIGHT);
        Creature creature = new Creature(movements);

        double time1 = 0;
        double time2 = 0;
        long s;
        for(int i = 0; i < 300000; i++)
        {
            s = System.nanoTime();
            environnement.calculateFinalPositionWithMap(creature, null);
            time1 = System.nanoTime() - s;
            s = System.nanoTime();
            environnement.calculateFinalPosition(creature, null);
            time2 = System.nanoTime() - s;
            System.out.println(i + " " + time1 + " " + time2);
        }
        System.out.println(time1 / 300000 + " " + time1);
        System.out.println(time2 / 300000 + " " + time2);



        //environnement.animate(creature, 500, false);
    }
}
