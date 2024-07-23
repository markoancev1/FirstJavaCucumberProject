package org.example.utils;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class FileReaderUtil {
    public static List<Map<String, String>> readFile(String filePath, List<String> columnNames) throws IOException, CsvException {
        if (filePath.endsWith(".csv")) {
            return readCSV(filePath, columnNames);
        } else if (filePath.endsWith(".xlsx")) {
            return readExcel(filePath, columnNames);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + filePath);
        }
    }

    private static List<Map<String, String>> readCSV(String filePath, List<String> columnNames) throws IOException, CsvException {
        List<Map<String, String>> columnData = new ArrayList<>();
        try (CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(new FileReader(filePath))) {
            Map<String, String> values;
            while ((values = csvReader.readMap()) != null) {
                Map<String, String> row = new HashMap<>();
                for (String columnName : columnNames) {
                    row.put(columnName, values.get(columnName));
                }
                columnData.add(row);
            }
        }
        return columnData;
    }

    private static List<Map<String, String>> readExcel(String filePath, List<String> columnNames) throws IOException {
        List<Map<String, String>> columnData = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (Cell cell : headerRow) {
                if (columnNames.contains(cell.getStringCellValue())) {
                    columnIndexMap.put(cell.getStringCellValue(), cell.getColumnIndex());
                }
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                Map<String, String> rowData = new HashMap<>();
                for (String columnName : columnNames) {
                    Cell cell = row.getCell(columnIndexMap.get(columnName));
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                rowData.put(columnName, cell.getStringCellValue());
                                break;
                            case NUMERIC:
                                rowData.put(columnName, String.valueOf(cell.getNumericCellValue()));
                                break;
                            case BOOLEAN:
                                rowData.put(columnName, String.valueOf(cell.getBooleanCellValue()));
                                break;
                            default:
                                rowData.put(columnName, "");
                        }
                    } else {
                        rowData.put(columnName, "");
                    }
                }
                columnData.add(rowData);
            }
        }
        return columnData;
    }
}