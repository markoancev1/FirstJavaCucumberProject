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

    private static final String DEFAULT_BROWSER = "chrome";
    private static final String DEFAULT_CONNECTION_TYPE = "3G";
    private static final String CONFIG_FILE_PATH = "config.json";

    static {
        // Load the JSON configuration at class loading time
        ObjectMapper mapper = new ObjectMapper();
        File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists()) {
            throw new RuntimeException("Configuration file not found at path: " + CONFIG_FILE_PATH);
        }
        try {
            configJson = mapper.readTree(configFile);
        } catch (IOException e) {
            logger.error("Failed to load network conditions JSON", e);
            throw new RuntimeException("Failed to load network conditions", e);
        }
    }

    @Before
    public synchronized void setupDriver() {
        if (driver == null) {
            String browser = System.getProperty("browser", DEFAULT_BROWSER);
            logger.info("Setting up WebDriver for {}", browser);
            try {
                initializeDriver(browser);

                if (driver instanceof ChromeDriver) {
                    logger.debug("ChromeDriver detected, setting up network conditions");
                    setupNetworkConditions(determineNetworkConditions());
                }

                String targetUrl = "https://practicetestautomation.com";
                logger.info("Navigating to {}", targetUrl);
                driver.get(targetUrl);
            } catch (Exception e) {
                logger.error("Failed to initialize WebDriver", e);
                throw new RuntimeException("WebDriver setup failed", e);
            }
        }
    }

    private void initializeDriver(String browser) {
        JsonNode browserConfig = configJson.path("browsers").path(browser.toLowerCase());

        if (browserConfig.isMissingNode()) {
            throw new RuntimeException("Unsupported browser: " + browser);
        }

        switch (browser.toLowerCase()) {
            case "firefox":
                logger.info("Initializing Firefox driver");
                driver = createFirefoxDriver(browserConfig);
                break;
            case "chrome":
            default:
                logger.info("Initializing Chrome driver");
                driver = createChromeDriver(browserConfig);
                break;
        }
        logger.info("WebDriver initialized for browser: {}", browser);
    }

    private WebDriver createFirefoxDriver(JsonNode browserConfig) {
        logger.info("Creating Firefox driver with specified options");
        FirefoxOptions options = new FirefoxOptions();

        if (browserConfig.path("headless").asBoolean(false)) {
            options.addArguments("--headless");
            logger.debug("Firefox running in headless mode");
        }

        options.addPreference("general.useragent.override", browserConfig.path("userAgent").asText(""));
        logger.debug("Set Firefox user agent to {}", browserConfig.path("userAgent").asText(""));

        for (JsonNode arg : browserConfig.path("additionalArgs")) {
            options.addArguments(arg.asText());
            logger.debug("Added Firefox argument: {}", arg.asText());
        }

        return new FirefoxDriver(options);
    }

    private WebDriver createChromeDriver(JsonNode browserConfig) {
        logger.info("Creating Chrome driver with specified options");
        ChromeOptions options = new ChromeOptions();

        if (browserConfig.path("headless").asBoolean(false)) {
            options.addArguments("--headless");
            logger.debug("Chrome running in headless mode");
        }

        options.addArguments("--user-agent=" + browserConfig.path("userAgent").asText(""));
        logger.debug("Set Chrome user agent to {}", browserConfig.path("userAgent").asText(""));

        for (JsonNode arg : browserConfig.path("additionalArgs")) {
            options.addArguments(arg.asText());
            logger.debug("Added Chrome argument: {}", arg.asText());
        }

        return new ChromeDriver(options);
    }

    private void setupNetworkConditions(NetworkConditions conditions) {
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
        logger.info("Network conditions set to {}: Latency = {} ms, Download = {} bps, Upload = {} bps, Packet Loss = {}%",
                conditions.connectionType, conditions.latency, conditions.downloadThroughput, conditions.uploadThroughput, conditions.packetLoss * 100);
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

    private NetworkConditions determineNetworkConditions() {
        String connectionParam = System.getProperty("connection", DEFAULT_CONNECTION_TYPE);
        NetworkConditions conditions = new NetworkConditions();
        JsonNode conditionNode = configJson.path("networkConditions").path(connectionParam.toUpperCase());

        if (conditionNode.isMissingNode()) {
            conditionNode = configJson.path("networkConditions").path(DEFAULT_CONNECTION_TYPE);
        }

        conditions.setNetworkConditions(
                ConnectionType.valueOf(conditionNode.path("connectionType").asText("")),
                conditionNode.path("latency").asInt(),
                conditionNode.path("downloadThroughput").asInt(),
                conditionNode.path("uploadThroughput").asInt(),
                conditionNode.path("packetLoss").asDouble()
        );

        logger.debug("Network conditions determined for {}: Latency = {} ms, Download = {} bps, Upload = {} bps, Packet Loss = {}%",
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