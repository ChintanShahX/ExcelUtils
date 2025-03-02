package com.cs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

// Main class to represent the JSON structure
public class JsonConfig {

    @JsonProperty("excelFilePath")
    private String excelFilePath;

    @JsonProperty("excelSheetName")
    private String excelSheetName;

    @JsonProperty("dataStartRow")
    private int dataStartRow;

    @JsonProperty("columns")
    private List<Column> columns;

    @JsonProperty("readColumnIds")
    private List<Integer> readColumns;

    // Getters and Setters

    public String getExcelFilePath() {
        return excelFilePath;
    }

    public int getDataStartRow() {
        return dataStartRow;
    }

    public String getExcelSheetName() {
        return excelSheetName;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Integer> getColumnIdsToRead() {
        return readColumns;
    }

    public void setReadColumns(List<Integer> readColumns) {
        this.readColumns = readColumns;
    }

    // Method to deserialize JSON file to Java object
    public static JsonConfig fromJsonFile(File jsonFile) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonFile, JsonConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Method to get position based on column name
    public int getPositionByName(String columnName) {
        if (columns != null) {
            for (Column column : columns) {
                if (column.getName().equalsIgnoreCase(columnName)) {
                    return column.getPosition();
                }
            }
        }
        return -1; // Return -1 if column name is not found
    }

    private static class Column {
        @JsonProperty("name")
        private String name;

        @JsonProperty("position")
        private int position;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }
}