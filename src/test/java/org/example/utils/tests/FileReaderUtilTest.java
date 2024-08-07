package org.example.utils.tests;

import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.utils.FileReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class FileReaderUtilTest {

    private static final List<String> COLUMN_NAMES = Arrays.asList("Name", "Age", "City");
    private static final Logger logger = LoggerFactory.getLogger(FileReaderUtilTest.class);
    private static Path filePath;

    @BeforeEach
    public void setUp() throws IOException {
        logger.info("Setting up test files...");
        filePath = Files.createTempFile("test", "");
        logger.info("Test files setup complete.");
    }

    static class FileTypeArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("csv", (Runnable) FileReaderUtilTest::createCSVFile),
                    Arguments.of("xlsx", (Runnable) FileReaderUtilTest::createExcelFile)
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(FileTypeArgumentsProvider.class)
    public void testReadValidFile(String fileType, Runnable fileCreationLogic) throws IOException, CsvException {
        logger.info("Starting testReadValidFile for {}...", fileType);
        fileCreationLogic.run();

        List<Map<String, String>> data = FileReaderUtil.readFile(filePath.toString() + "." + fileType, COLUMN_NAMES);

        assertNotNull(data, "Data should not be null");
        assertEquals(2, data.size(), "Expected two rows of data");

        assertRowEquals(data.get(0), "John", "30", "New York");
        assertRowEquals(data.get(1), "Jane", "25", "Los Angeles");

        logger.info("testReadValidFile completed successfully for {}.", fileType);
    }

    @ParameterizedTest
    @ArgumentsSource(FileTypeArgumentsProvider.class)
    public void testFileWithMissingColumns(String fileType) throws IOException, CsvException {
        logger.info("Starting testFileWithMissingColumns for {}...", fileType);

        if ("csv".equals(fileType)) {
            Files.write(filePath.resolveSibling(filePath.getFileName().toString() + ".csv"), Arrays.asList(
                    "Name,Age",
                    "Alice,28"
            ));
        } else if ("xlsx".equals(fileType)) {
            try (Workbook workbook = new XSSFWorkbook();
                 FileOutputStream fileOut = new FileOutputStream(filePath.resolveSibling(filePath.getFileName().toString() + ".xlsx").toFile())) {
                Sheet sheet = workbook.createSheet();
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Name");
                headerRow.createCell(1).setCellValue("Age");

                Row dataRow = sheet.createRow(1);
                dataRow.createCell(0).setCellValue("Alice");
                dataRow.createCell(1).setCellValue("28");

                workbook.write(fileOut);
            }
        }

        List<Map<String, String>> data = FileReaderUtil.readFile(filePath.toString() + "." + fileType, COLUMN_NAMES);

        assertNotNull(data, "Data should not be null");
        assertEquals(1, data.size(), "Expected one row of data");
        assertRowEquals(data.getFirst(), "Alice", "28", null);

        logger.info("testFileWithMissingColumns completed successfully for {}.", fileType);
    }

    @ParameterizedTest
    @ArgumentsSource(FileTypeArgumentsProvider.class)
    public void testEmptyFile(String fileType) throws IOException, CsvException {
        logger.info("Starting testEmptyFile for {}...", fileType);

        if ("csv".equals(fileType)) {
            Files.write(filePath.resolveSibling(filePath.getFileName().toString() + ".csv"), List.of("Name,Age,City"));
        } else if ("xlsx".equals(fileType)) {
            try (Workbook workbook = new XSSFWorkbook();
                 FileOutputStream fileOut = new FileOutputStream(filePath.resolveSibling(filePath.getFileName().toString() + ".xlsx").toFile())) {
                Sheet sheet = workbook.createSheet();
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Name");
                headerRow.createCell(1).setCellValue("Age");
                headerRow.createCell(2).setCellValue("City");

                workbook.write(fileOut);
            }
        }

        List<Map<String, String>> data = FileReaderUtil.readFile(filePath.toString() + "." + fileType, COLUMN_NAMES);

        assertNotNull(data, "Data should not be null");
        assertTrue(data.isEmpty(), "Expected the data to be empty for an empty " + fileType);

        logger.info("testEmptyFile completed successfully for {}.", fileType);
    }

    @ParameterizedTest
    @ArgumentsSource(FileTypeArgumentsProvider.class)
    public void testFileWithAdditionalColumns(String fileType) throws IOException, CsvException {
        logger.info("Starting testFileWithAdditionalColumns for {}...", fileType);

        if ("csv".equals(fileType)) {
            Files.write(filePath.resolveSibling(filePath.getFileName().toString() + ".csv"), List.of(
                    "Name,Age,City,ExtraColumn1,ExtraColumn2",
                    "Alice,30,New York,SomeValue1,SomeValue2"
            ));
        } else if ("xlsx".equals(fileType)) {
            try (Workbook workbook = new XSSFWorkbook();
                 FileOutputStream fileOut = new FileOutputStream(filePath.resolveSibling(filePath.getFileName().toString() + ".xlsx").toFile())) {
                Sheet sheet = workbook.createSheet();
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Name");
                headerRow.createCell(1).setCellValue("Age");
                headerRow.createCell(2).setCellValue("City");
                headerRow.createCell(3).setCellValue("ExtraColumn1");
                headerRow.createCell(4).setCellValue("ExtraColumn2");

                Row dataRow = sheet.createRow(1);
                dataRow.createCell(0).setCellValue("Alice");
                dataRow.createCell(1).setCellValue("30");
                dataRow.createCell(2).setCellValue("New York");
                dataRow.createCell(3).setCellValue("SomeValue1");
                dataRow.createCell(4).setCellValue("SomeValue2");

                workbook.write(fileOut);
            }
        }

        List<Map<String, String>> data = FileReaderUtil.readFile(filePath.toString() + "." + fileType, COLUMN_NAMES);

        assertNotNull(data, "Data should not be null");
        assertEquals(1, data.size(), "Expected one row of data");
        assertEquals("Alice", data.getFirst().get("Name"));
        assertEquals("30", data.getFirst().get("Age"));
        assertEquals("New York", data.getFirst().get("City"));
        assertNull(data.getFirst().get("ExtraColumn1"), "Extra columns should not be present");
        assertNull(data.getFirst().get("ExtraColumn2"), "Extra columns should not be present");

        logger.info("testFileWithAdditionalColumns completed successfully for {}.", fileType);
    }

    @Test
    public void testNonExistentFile() {
        logger.info("Starting testNonExistentFile...");
        assertThrows(IOException.class, () -> FileReaderUtil.readFile("non_existent_file.csv", COLUMN_NAMES));
        logger.info("testNonExistentFile completed successfully.");
    }

    @Test
    public void testInvalidCSVHeaderFormat() throws IOException {
        logger.info("Starting testInvalidCSVHeaderFormat...");

        Path invalidCsvPath = Files.createTempFile("invalid", ".csv");
        Files.write(invalidCsvPath, Arrays.asList("InvalidHeader1,InvalidHeader2", "SomeData1,SomeData2"));

        assertThrows(CsvException.class, () -> FileReaderUtil.readFile(invalidCsvPath.toString(), COLUMN_NAMES));

        logger.info("testInvalidCSVHeaderFormat completed successfully.");
    }

    @Test
    public void testMalformedCSVContent() throws IOException, CsvException {
        logger.info("Starting testMalformedCSVContent...");
        Path malformedCsvPath = Files.createTempFile("malformed", ".csv");
        Files.write(malformedCsvPath, Arrays.asList(
                "Name,Age,City",
                "John,30",
                "Jane,25,Los Angeles,Extra"
        ));

        List<Map<String, String>> data = FileReaderUtil.readFile(malformedCsvPath.toString(), COLUMN_NAMES);

        assertNotNull(data, "Data should not be null");
        assertEquals(2, data.size(), "Expected two rows of data");
        assertRowEquals(data.get(0), "John", "30", null);
        assertRowEquals(data.get(1), "Jane", "25", "Los Angeles");

        logger.info("testMalformedCSVContent completed successfully.");
    }

    @Test
    public void testSpecialCharactersInCSV() throws IOException, CsvException {
        logger.info("Starting testSpecialCharactersInCSV...");
        Path specialCharCsvPath = Files.createTempFile("specialChar", ".csv");
        Files.write(specialCharCsvPath, Arrays.asList(
                "Name,Age,City",
                "Jöhn,30,New York",
                "Marie,25,Los Ángeles"
        ));

        List<Map<String, String>> data = FileReaderUtil.readFile(specialCharCsvPath.toString(), COLUMN_NAMES);

        assertNotNull(data, "Data should not be null");
        assertEquals(2, data.size(), "Expected two rows of data");
        assertRowEquals(data.get(0), "Jöhn", "30", "New York");
        assertRowEquals(data.get(1), "Marie", "25", "Los Ángeles");

        logger.info("testSpecialCharactersInCSV completed successfully.");
    }

    @Test
    public void testLargeCSVFile() throws IOException, CsvException {
        logger.info("Starting testLargeCSVFile...");
        Path largeCsvPath = Files.createTempFile("large", ".csv");
        try (var writer = Files.newBufferedWriter(largeCsvPath)) {
            writer.write("Name,Age,City\n");
            for (int i = 0; i < 10000; i++) {
                writer.write("Name" + i + "," + (20 + i) + ",City" + i + "\n");
            }
        }

        List<Map<String, String>> data = FileReaderUtil.readFile(largeCsvPath.toString(), COLUMN_NAMES);

        assertNotNull(data, "Data should not be null");
        assertEquals(10000, data.size(), "Expected 10000 rows of data");
        assertRowEquals(data.get(9999), "Name9999", "10019", "City9999");

        logger.info("testLargeCSVFile completed successfully.");
    }

    private static void createCSVFile() {
        try {
            Files.write(filePath.resolveSibling(filePath.getFileName().toString() + ".csv"), Arrays.asList(
                    "Name,Age,City",
                    "John,30,New York",
                    "Jane,25,Los Angeles"
            ));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void testLargeExcelFile(@TempDir Path tempDir) throws IOException, CsvException {
        System.out.println("Starting testLargeExcelFile...");

        File largeExcelFile = createLargeExcelFile(tempDir.resolve("large_test.xlsx").toFile());

        List<Map<String, String>> data = FileReaderUtil.readFile(largeExcelFile.getAbsolutePath(), COLUMN_NAMES);

        assertNotNull(data, "Data should not be null");
        assertEquals(10000, data.size(), "Expected 10,000 rows of data");

        Map<String, String> firstRow = data.getFirst();
        assertEquals("Name 0", firstRow.get("Name"));
        assertEquals("0", parseNumericString(firstRow.get("Age")), "Age mismatch for first row");
        assertEquals("City 0", firstRow.get("City"));

        Map<String, String> lastRow = data.get(9999);
        assertEquals("Name 9999", lastRow.get("Name"));
        assertEquals("9999", parseNumericString(lastRow.get("Age")), "Age mismatch for last row");
        assertEquals("City 9999", lastRow.get("City"));

        System.out.println("testLargeExcelFile completed successfully.");
    }

    private File createLargeExcelFile(File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test Sheet");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Age");
            headerRow.createCell(2).setCellValue("City");

            for (int i = 0; i < 10000; i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue("Name " + i);
                row.createCell(1).setCellValue(i);
                row.createCell(2).setCellValue("City " + i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
        }
        return file;
    }

    private static void createExcelFile() {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream(filePath.resolveSibling(filePath.getFileName().toString() + ".xlsx").toFile())) {
            Sheet sheet = workbook.createSheet();
            createHeaderRow(sheet);
            addDataRow(sheet, 1, Arrays.asList("John", "30", "New York"));
            addDataRow(sheet, 2, Arrays.asList("Jane", "25", "Los Angeles"));
            workbook.write(fileOut);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < COLUMN_NAMES.size(); i++) {
            headerRow.createCell(i).setCellValue(COLUMN_NAMES.get(i));
        }
    }

    private String parseNumericString(String value) {
        if (value.contains(".")) {
            double doubleValue = Double.parseDouble(value);
            int intValue = (int) doubleValue;
            return String.valueOf(intValue);
        }
        return value;
    }

    private static void addDataRow(Sheet sheet, int rowIndex, List<String> values) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.size(); i++) {
            row.createCell(i).setCellValue(values.get(i));
        }
    }

    private void assertRowEquals(Map<String, String> row, String name, String age, String city) {
        assertEquals(name, row.get("Name"), "Name mismatch");
        assertEquals(age, row.get("Age"), "Age mismatch");
        assertEquals(city, row.get("City"), "City mismatch");
    }
}