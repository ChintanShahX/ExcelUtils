package com.cs;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ExcelUtils {

    private static final ThreadLocal<Workbook> threadLocalWorkbook = new ThreadLocal<>();
    private static final ReentrantLock lock = new ReentrantLock();
    private final String excelFilePath;

    public String getExcelSheetName() {
        return excelSheetName;
    }

    private final String excelSheetName;
    private static ThreadLocal<JsonConfig> threadLocalJsonColumnConfig = new ThreadLocal<>();

    private JsonConfig getJsonConfig() {
        return threadLocalJsonColumnConfig.get();
    }

    public static ExcelUtils loadConfig(String filePath) {
        File configFile = FileReaderUtil.getFileFromLocation(filePath);
        return new ExcelUtils(configFile);
    }

    private ExcelUtils(File configFile) {

        JsonConfig jsonConfig = JsonConfig.fromJsonFile(configFile);
        this.excelFilePath = jsonConfig.getExcelFilePath();
        this.excelSheetName = jsonConfig.getExcelSheetName();
        threadLocalJsonColumnConfig.set(jsonConfig);
    }

    public int getColumnPosition(String columnName) {
        return threadLocalJsonColumnConfig.get().getPositionByName(columnName);
    }

    private Workbook getWorkbook() {

        Workbook workbook = threadLocalWorkbook.get();
        if (workbook == null) {
            synchronized (this) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(FileReaderUtil.getFileFromLocation(excelFilePath));
                    workbook = new XSSFWorkbook(fis);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                threadLocalWorkbook.set(workbook);
            }
        }
        return workbook;
    }


    public Object[][] getTable() {

        List<Integer> validColumns = threadLocalJsonColumnConfig.get().getColumnIdsToRead();
        Workbook workbook = getWorkbook();
        Sheet sheet = workbook.getSheet(excelSheetName);
        if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + excelSheetName);

        int rowCount = sheet.getPhysicalNumberOfRows();
        int colCount = validColumns.size();

        String[][] data = new String[rowCount - 1][colCount]; // Exclude header

        for (int i = getJsonConfig().getDataStartRow(); i < rowCount; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            for (int j = 0; j < colCount; j++) {
                if (validColumns.contains(j)) {
                    Cell cell = row.getCell(j);
                    data[i - 1][j] = (cell != null) ? getCellValueAsString(cell) : "";
                }
            }
        }
        return data;
    }

    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public void writeExecutionStatus(String sheetName, int rowNum, int colNum, String status) {
        lock.lock();
        try {
            Workbook workbook = getWorkbook();
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + sheetName);

            Row row = sheet.getRow(rowNum);
            if (row == null) row = sheet.createRow(rowNum);
            Cell cell = row.createCell(colNum);
            cell.setCellValue(status);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(FileReaderUtil.getFileFromLocation(excelFilePath));
                workbook.write(fos);
                fos.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            lock.unlock();
        }
    }

    public void closeWorkbook() {
        Workbook workbook = threadLocalWorkbook.get();
        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            threadLocalWorkbook.remove();
        }
    }
}