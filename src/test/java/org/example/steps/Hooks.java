package org.example.steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hooks {

    private static WebDriver driver;
    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);

    @Before
    public void setupDriver() {
        if (driver == null) {
            String browser = System.getProperty("browser", "chrome");
            logger.info("Setting up WebDriver for {}", browser);

            driver = browser.equalsIgnoreCase("firefox") ? createFirefoxDriver() : createChromeDriver();

            logger.info("Navigating to https://practicetestautomation.com");
            driver.get("https://practicetestautomation.com");
        }
    }

    private WebDriver createFirefoxDriver() {
        logger.info("Creating Firefox driver with headless options");
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        return new FirefoxDriver(options);
    }

    private WebDriver createChromeDriver() {
        logger.info("Creating Chrome driver with headless options");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--headless");
        return new ChromeDriver(options);
    }

    @After
    public void tearDown() {
        if (driver != null) {
            logger.info("Quitting WebDriver");
            driver.quit();
            driver = null;
        }
    }

    public static WebDriver getDriver() {
        return driver;
    }
}