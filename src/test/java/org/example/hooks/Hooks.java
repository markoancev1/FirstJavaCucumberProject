package org.example.hooks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Hooks {

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);
    private static WebDriver driver;
    private static final JsonNode configJson;

    static {
        // Load the JSON configuration at class loading time
        ObjectMapper mapper = new ObjectMapper();
        try {
            configJson = mapper.readTree(new File("config.json"));
        } catch (IOException e) {
            logger.error("Failed to load network conditions JSON", e);
            throw new RuntimeException("Failed to load network conditions", e);
        }
    }

    @Before
    public synchronized void setupDriver() {
        if (driver == null) {
            String browser = System.getProperty("browser", "chrome");
            logger.info("Setting up WebDriver for {}", browser);
            try {
                initializeDriver(browser);

                if (driver instanceof ChromeDriver) {
                    String connectionTypeParam = System.getProperty("connection", "3G");
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
        JsonNode browserConfig = configJson.get("browsers").get(browser.toLowerCase());

        if (browserConfig == null) {
            throw new RuntimeException("Unsupported browser: " + browser);
        }

        if ("firefox".equalsIgnoreCase(browser)) {
            driver = createFirefoxDriver(browserConfig);
        } else {
            driver = createChromeDriver(browserConfig);
        }
        logger.info("WebDriver initialized for browser: {}", browser);
    }

    private WebDriver createFirefoxDriver(JsonNode browserConfig) {
        logger.info("Creating Firefox driver with specified options");
        FirefoxOptions options = new FirefoxOptions();

        if (browserConfig.get("headless").asBoolean()) {
            options.addArguments("--headless");
        }

        options.addPreference("general.useragent.override", browserConfig.get("userAgent").asText());

        for (JsonNode arg : browserConfig.get("additionalArgs")) {
            options.addArguments(arg.asText());
        }

        return new FirefoxDriver(options);
    }

    private WebDriver createChromeDriver(JsonNode browserConfig) {
        logger.info("Creating Chrome driver with specified options");
        ChromeOptions options = new ChromeOptions();

        if (browserConfig.get("headless").asBoolean()) {
            options.addArguments("--headless");
        }

        options.addArguments("--user-agent=" + browserConfig.get("userAgent").asText());

        for (JsonNode arg : browserConfig.get("additionalArgs")) {
            options.addArguments(arg.asText());
        }

        return new ChromeDriver(options);
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
        JsonNode conditionNode = configJson.get("networkConditions").get(connectionParam.toUpperCase());
        if (conditionNode == null) {
            conditionNode = configJson.get("networkConditions").get("3G"); // Default to 3G if not found
        }

        conditions.setNetworkConditions(
                ConnectionType.valueOf(conditionNode.get("connectionType").asText()),
                conditionNode.get("latency").asInt(),
                conditionNode.get("downloadThroughput").asInt(),
                conditionNode.get("uploadThroughput").asInt(),
                conditionNode.get("packetLoss").asDouble()
        );

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