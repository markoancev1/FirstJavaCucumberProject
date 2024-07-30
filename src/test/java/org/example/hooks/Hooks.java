package org.example.hooks;

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

    /**
     * Sets up the WebDriver before each test.
     * Configures the WebDriver based on the specified browser type.
     */
    @Before
    public synchronized void setupDriver() {
        if (driver == null) {
            String browser = System.getProperty("browser", "chrome");
            logger.info("Setting up WebDriver for {}", browser);

            try {
                driver = browser.equalsIgnoreCase("firefox") ? createFirefoxDriver() : createChromeDriver();
                logger.info("Navigating to https://practicetestautomation.com");
                driver.get("https://practicetestautomation.com");
            } catch (Exception e) {
                logger.error("Failed to create WebDriver", e);
                throw new RuntimeException("WebDriver setup failed", e);
            }
        }
    }

    /**
     * Creates a Firefox WebDriver with headless options.
     *
     * @return a configured Firefox WebDriver.
     */
    private WebDriver createFirefoxDriver() {
        logger.info("Creating Firefox driver with headless options");
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        return new FirefoxDriver(options);
    }

    /**
     * Creates a Chrome WebDriver with headless options.
     *
     * @return a configured Chrome WebDriver.
     */
    private WebDriver createChromeDriver() {
        logger.info("Creating Chrome driver with headless options");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--headless");
        return new ChromeDriver(options);
    }

    /**
     * Tears down the WebDriver after each test.
     * Quits the WebDriver and sets the instance to null.
     */
    @After
    public synchronized void tearDown() {
        if (driver != null) {
            logger.info("Quitting WebDriver");
            try {
                driver.quit();
            } catch (Exception e) {
                logger.error("Failed to quit WebDriver", e);
            } finally {
                driver = null;
            }
        }
    }

    /**
     * Returns the WebDriver instance.
     *
     * @return the WebDriver instance.
     */
    public static synchronized WebDriver getDriver() {
        return driver;
    }
}