package org.example.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v127.network.Network;
import org.openqa.selenium.devtools.v127.network.model.ConnectionType;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Hooks {

    private static WebDriver driver;
    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);

    @Before
    public synchronized void setupDriver() {
        if (driver == null) {
            String browser = System.getProperty("browser", "chrome");
            String connectionTypeParam = System.getProperty("connection", "3G"); // Default to 3G
            NetworkConditions conditions = determineNetworkConditions(connectionTypeParam);

            logger.info("Setting up WebDriver for {} with {} connection", browser, connectionTypeParam);
            try {
                initializeDriver(browser);
                setupNetworkConditions(conditions);
                driver.get("https://practicetestautomation.com");
            } catch (Exception e) {
                logger.error("Failed to initialize WebDriver", e);
                throw new RuntimeException("WebDriver setup failed", e);
            }
        }
    }

    private void initializeDriver(String browser) {
        if ("firefox".equalsIgnoreCase(browser)) {
            driver = createFirefoxDriver();
        } else {
            driver = createChromeDriver();
        }
        logger.info("WebDriver initialized for browser: {}", browser);
    }

    private void setupNetworkConditions(NetworkConditions conditions) {
        if (driver instanceof ChromeDriver) {
            DevTools devTools = ((ChromeDriver) driver).getDevTools();
            try {
                devTools.createSession();
                devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
                devTools.send(Network.emulateNetworkConditions(
                        false, conditions.latency, conditions.downloadThroughput, conditions.uploadThroughput,
                        Optional.of(conditions.connectionType), Optional.of(conditions.packetLoss),
                        Optional.of(0), Optional.of(false)));
                logger.info("Network conditions set to {}", conditions.connectionType);
            } catch (Exception e) {
                logger.error("Failed to set network conditions", e);
                throw new RuntimeException("Network condition setup failed", e);
            }
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

    public static synchronized WebDriver getDriver() {
        return driver;
    }

    private NetworkConditions determineNetworkConditions(String connectionParam) {
        NetworkConditions conditions = new NetworkConditions();
        switch (connectionParam.toUpperCase()) {
            case "2G":
                conditions.connectionType = ConnectionType.CELLULAR2G;
                conditions.latency = 500; // High latency
                conditions.downloadThroughput = 250 * 1024; // Very slow download speed
                conditions.uploadThroughput = 50 * 1024; // Very slow upload speed
                conditions.packetLoss = 0.5; // 0.5% packet loss
                break;
            case "3G":
                conditions.connectionType = ConnectionType.CELLULAR3G;
                conditions.latency = 250; // Moderate latency
                conditions.downloadThroughput = 750 * 1024; // Moderate download speed
                conditions.uploadThroughput = 250 * 1024; // Moderate upload speed
                conditions.packetLoss = 0.2; // 0.2% packet loss
                break;
            case "4G":
                conditions.connectionType = ConnectionType.CELLULAR4G;
                conditions.latency = 100; // Lower latency
                conditions.downloadThroughput = 4000 * 1024; // Fast download speed
                conditions.uploadThroughput = 3000 * 1024; // Fast upload speed
                conditions.packetLoss = 0.1; // Minimal packet loss
                break;
            default:
                // Default to 3G if unknown
                conditions.connectionType = ConnectionType.CELLULAR3G;
                conditions.latency = 250;
                conditions.downloadThroughput = 750 * 1024;
                conditions.uploadThroughput = 250 * 1024;
                conditions.packetLoss = 0.2;
                break;
        }
        return conditions;
    }

    // NetworkConditions class to hold network parameters
    static class NetworkConditions {
        ConnectionType connectionType;
        int latency;
        int downloadThroughput;
        int uploadThroughput;
        double packetLoss;
    }
}