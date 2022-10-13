package com.arthur.hepl;

import java.io.FileWriter;
import java.io.IOException;

public class DataStorage
{
    private static String filepath = "data.csv";
    private static FileWriter writer;

    static
    {
        try
        {
            writer = new FileWriter(filepath);
            writer.write("pop_id;ind_id;fitness\n");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void store(int i, int j, int fitness)
    {
        try
        {
            writer.write(i + ";" + j + ";" + fitness + "\n");
            writer.flush();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
