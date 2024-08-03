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

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);
    private static WebDriver driver;

    @Before
    public synchronized void setupDriver() {
        if (driver == null) {
            String browser = System.getProperty("browser", "chrome");
            logger.info("Setting up WebDriver for {}", browser);
            try {
                initializeDriver(browser);

                // Only set network conditions if the driver is a ChromeDriver
                if (driver instanceof ChromeDriver) {
                    String connectionTypeParam = System.getProperty("connection", "3G"); // Default to 3G
                    NetworkConditions conditions = determineNetworkConditions(connectionTypeParam);
                    setupNetworkConditions(conditions);
                }

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
            devTools.createSession();
            devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
            devTools.send(Network.emulateNetworkConditions(
                    false,
                    conditions.latency,
                    conditions.downloadThroughput,
                    conditions.uploadThroughput,
                    Optional.of(conditions.connectionType),
                    Optional.of(conditions.packetLoss),
                    Optional.of(0),
                    Optional.of(false)
            ));
            logger.info("Network conditions set to {}", conditions.connectionType);
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
                conditions.setNetworkConditions(ConnectionType.CELLULAR2G, 500, 250 * 1024, 50 * 1024, 0.5);
                break;
            case "4G":
                conditions.setNetworkConditions(ConnectionType.CELLULAR4G, 100, 4000 * 1024, 3000 * 1024, 0.1);
                break;
            case "WIFI":
                conditions.setNetworkConditions(ConnectionType.WIFI, 30, 10000 * 1024, 5000 * 1024, 0.01);
                break;
            case "ETHERNET":
                conditions.setNetworkConditions(ConnectionType.ETHERNET, 20, 100000 * 1024, 50000 * 1024, 0.005);
                break;
            default:
                conditions.setNetworkConditions(ConnectionType.CELLULAR3G, 250, 750 * 1024, 250 * 1024, 0.2);
                break;
        }
        logger.debug("Network conditions set for {}: Latency = {} ms, Download = {} bps, Upload = {} bps, Packet Loss = {}%",
                connectionParam, conditions.latency, conditions.downloadThroughput, conditions.uploadThroughput, conditions.packetLoss * 100);
        return conditions;
    }

    // NetworkConditions class to hold network parameters
    static class NetworkConditions {
        ConnectionType connectionType;
        int latency;
        int downloadThroughput;
        int uploadThroughput;
        double packetLoss;

        void setNetworkConditions(ConnectionType type, int latency, int download, int upload, double packetLoss) {
            this.connectionType = type;
            this.latency = latency;
            this.downloadThroughput = download;
            this.uploadThroughput = upload;
            this.packetLoss = packetLoss;
        }
    }
}