package org.example.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FileReaderUtils {
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
        try (CSVReader csvReader = new CSVReader(new FileReader(filePath))) {
            // Read headers
            String[] actualHeaders = csvReader.readNext();
            if (actualHeaders == null) {
                throw new CsvException("CSV file is empty");
            }

            List<String> actualHeaderList = Arrays.asList(actualHeaders);

            // Ensure at least one expected header is present to consider it a valid CSV format
            boolean validFormat = false;
            for (String expectedHeader : columnNames) {
                if (actualHeaderList.contains(expectedHeader)) {
                    validFormat = true;
                    break;
                }
            }

            if (!validFormat) {
                throw new CsvException("Invalid CSV header format");
            }

            // Read data
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                Map<String, String> row = new HashMap<>();
                for (String columnName : columnNames) {
                    int index = actualHeaderList.indexOf(columnName);
                    if (index != -1 && index < values.length) {
                        row.put(columnName, values[index]);
                    } else {
                        row.put(columnName, null);  // Handle missing columns by setting null
                    }
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

            // Check if the sheet is empty or does not exist
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return columnData; // Return empty list
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return columnData; // Return empty list if there's no header
            }

            // Map column names to indices
            Map<String, Integer> headerMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String headerName = cell.getStringCellValue();
                if (columnNames.contains(headerName)) {
                    headerMap.put(headerName, cell.getColumnIndex());
                }
            }

            // Read data rows
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, String> rowData = new HashMap<>();
                for (String columnName : columnNames) {
                    Integer colIndex = headerMap.get(columnName);
                    if (colIndex != null) {
                        Cell cell = row.getCell(colIndex);
                        if (cell != null) {
                            rowData.put(columnName, cell.toString());
                        } else {
                            rowData.put(columnName, null); // Handle missing cells
                        }
                    } else {
                        rowData.put(columnName, null); // Handle missing columns
                    }
                }
                columnData.add(rowData);
            }
        }
        return columnData;
    }
}