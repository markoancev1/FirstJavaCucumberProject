package org.example.steps;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.Given;
import org.openqa.selenium.WebDriver;
import org.junit.Assert;
import org.example.pages.HomePage;
import org.example.utils.FileReaderUtil;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
public class homesteps {

    WebDriver driver;
    HomePage homePage;

    @Given("I have a configured Cucumber-JVM project")
    public void iHaveAConfiguredCucumberJVMProject() {
        driver = Hooks.getDriver();
        homePage = new HomePage(driver);
        Assert.assertNotNull(driver);
        Assert.assertNotNull(homePage);
    }

    @When("I run the tests")
    public void iRunTheTests() {
        homePage.checkImageVisibility();
    }

    @Then("they should pass without any failures")
    public void theyShouldPassWithoutAnyFailures() {
        driver.quit();
    }

    @When("I read data from file {string} column {string}")
    public void iReadDataFromFileColumn(String filePath, String columnName) throws IOException, CsvException {
        List<String> columnNames = Arrays.asList(columnName.split(","));

        List<Map<String, String>> columnData = FileReaderUtil.readFile(filePath, columnNames);
        for (Map<String, String> row : columnData) {
            for (String columnNameString : columnNames) {
                String emailString = row.get(columnNameString);
                Assert.assertNotNull(emailString);
            }
        }
    }
}