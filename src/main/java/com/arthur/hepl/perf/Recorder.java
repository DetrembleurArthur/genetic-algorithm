package com.arthur.hepl.perf;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Recorder
{
    private final File recordFile;
    private final ArrayList<String> columnNames = new ArrayList<>();
    private final ArrayList<ArrayList<String>> data = new ArrayList<>();

    @SneakyThrows
    public Recorder(String filename)
    {
        recordFile = new File(filename);
    }

    public void setColumns(String ... colNames)
    {
        columnNames.addAll(List.of(colNames));
    }

    @SneakyThrows
    public void write()
    {
        FileWriter writer = new FileWriter(recordFile);
        final StringBuilder buffer = new StringBuilder();
        for(String colName : columnNames)
        {
            buffer.append(colName).append(";");
        }
        buffer.deleteCharAt(buffer.lastIndexOf(";"));
        buffer.append("\n");
        for(ArrayList<String> row : data)
        {
            for(String value : row)
            {
                buffer.append(value).append(";");
            }
            buffer.deleteCharAt(buffer.lastIndexOf(";"));
            buffer.append("\n");
        }
        writer.write(buffer.toString());
        writer.flush();
        writer.close();
    }

    public void addRow(String ... row)
    {
        ArrayList<String> rowList = new ArrayList<>(Arrays.asList(row));
        this.data.add(rowList);
    }
}
