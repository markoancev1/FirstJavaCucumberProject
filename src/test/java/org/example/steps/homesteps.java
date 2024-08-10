package org.example.steps;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.Given;
import org.example.hooks.Hooks;
import org.example.utils.FileReaderUtils;
import org.openqa.selenium.WebDriver;
import org.junit.jupiter.api.Assertions;
import org.example.pages.HomePage;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class homesteps {

    private static final Logger logger = LoggerFactory.getLogger(homesteps.class);

    WebDriver driver;
    HomePage homePage;

    @Given("I have a configured Cucumber-JVM project")
    public void iHaveAConfiguredCucumberJVMProject() {
        logger.info("Initializing WebDriver and HomePage for the Cucumber-JVM project");
        driver = Hooks.getDriver();
        homePage = new HomePage(driver);
        Assertions.assertNotNull(driver, "WebDriver should not be null");
        Assertions.assertNotNull(homePage, "HomePage should not be null");
        logger.info("WebDriver and HomePage initialized successfully");
    }

    @When("I run the tests")
    public void iRunTheTests() {
        logger.info("Executing test to check image visibility");
        homePage.checkImageVisibility();
    }

    @Then("they should pass without any failures")
    public void theyShouldPassWithoutAnyFailures() {
        logger.info("Tests completed, quitting WebDriver");
        driver.quit();
    }

    @When("I read data from file {string} column {string}")
    public void iReadDataFromFileColumn(String filePath, String columnName) throws IOException, CsvException {
        logger.info("Reading data from file: {} for column(s): {}", filePath, columnName);
        List<String> columnNames = Arrays.asList(columnName.split(","));

        List<Map<String, String>> columnData = FileReaderUtils.readFile(filePath, columnNames);
        for (Map<String, String> row : columnData) {
            for (String columnNameString : columnNames) {
                String emailString = row.get(columnNameString);
                Assertions.assertNotNull(emailString, "Email string should not be null");
                logger.debug("Read value '{}' for column '{}'", emailString, columnNameString);
            }
        }
        logger.info("Completed reading data from file");
    }
}