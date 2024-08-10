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

    private static final Logger LOGGER = LoggerFactory.getLogger(Hooks.class);
    private static WebDriver driver;
    private static final JsonNode CONFIG_JSON;

    private static final String DEFAULT_BROWSER = "chrome";
    private static final String DEFAULT_CONNECTION_TYPE = "3G";
    private static final String CONFIG_FILE_PATH = "config.json";
    private static final String TARGET_URL = "https://practicetestautomation.com";

    static {
        CONFIG_JSON = loadConfigJson();
    }

    @Before
    public synchronized void setupDriver() {
        if (driver == null) {
            String browser = System.getProperty("browser", DEFAULT_BROWSER);
            LOGGER.info("Setting up WebDriver for {}", browser);
            try {
                initializeDriver(browser);
                setupNetworkConditionsIfChrome();
                navigateToTargetUrl();
            } catch (Exception e) {
                LOGGER.error("Failed to initialize WebDriver", e);
                throw new RuntimeException("WebDriver setup failed", e);
            }
        }
    }

    private static JsonNode loadConfigJson() {
        ObjectMapper mapper = new ObjectMapper();
        File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists()) {
            throw new RuntimeException("Configuration file not found at path: " + CONFIG_FILE_PATH);
        }
        try {
            return mapper.readTree(configFile);
        } catch (IOException e) {
            LOGGER.error("Failed to load network conditions JSON", e);
            throw new RuntimeException("Failed to load network conditions", e);
        }
    }

    private void initializeDriver(String browser) {
        JsonNode browserConfig = CONFIG_JSON.path("browsers").path(browser.toLowerCase());
        if (browserConfig.isMissingNode()) {
            throw new RuntimeException("Unsupported browser: " + browser);
        }
        driver = "firefox".equalsIgnoreCase(browser) ? createFirefoxDriver(browserConfig) : createChromeDriver(browserConfig);
        LOGGER.info("WebDriver initialized for browser: {}", browser);
    }

    private WebDriver createFirefoxDriver(JsonNode browserConfig) {
        LOGGER.info("Creating Firefox driver with specified options");
        FirefoxOptions options = new FirefoxOptions();
        setCommonBrowserOptions(options, browserConfig);
        return new FirefoxDriver(options);
    }

    private WebDriver createChromeDriver(JsonNode browserConfig) {
        LOGGER.info("Creating Chrome driver with specified options");
        ChromeOptions options = new ChromeOptions();
        setCommonBrowserOptions(options, browserConfig);
        return new ChromeDriver(options);
    }

    private void setCommonBrowserOptions(Object options, JsonNode browserConfig) {
        if (browserConfig.path("headless").asBoolean(false)) {
            if (options instanceof FirefoxOptions) {
                ((FirefoxOptions) options).addArguments("--headless");
            } else if (options instanceof ChromeOptions) {
                ((ChromeOptions) options).addArguments("--headless");
            }
            LOGGER.debug("Browser running in headless mode");
        }

        String userAgent = browserConfig.path("userAgent").asText("");
        if (options instanceof FirefoxOptions) {
            ((FirefoxOptions) options).addPreference("general.useragent.override", userAgent);
        } else if (options instanceof ChromeOptions) {
            ((ChromeOptions) options).addArguments("--user-agent=" + userAgent);
        }
        LOGGER.debug("Set user agent to {}", userAgent);

        browserConfig.path("additionalArgs").forEach(arg -> {
            String argText = arg.asText();
            if (options instanceof FirefoxOptions) {
                ((FirefoxOptions) options).addArguments(argText);
            } else if (options instanceof ChromeOptions) {
                ((ChromeOptions) options).addArguments(argText);
            }
            LOGGER.debug("Added browser argument: {}", argText);
        });
    }

    private void setupNetworkConditionsIfChrome() {
        if (driver instanceof ChromeDriver) {
            LOGGER.debug("ChromeDriver detected, setting up network conditions");
            setupNetworkConditions(determineNetworkConditions());
        }
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
        LOGGER.info("Network conditions set to {}: Latency = {} ms, Download = {} bps, Upload = {} bps, Packet Loss = {}%",
                conditions.connectionType, conditions.latency, conditions.downloadThroughput, conditions.uploadThroughput, conditions.packetLoss * 100);
    }

    private void navigateToTargetUrl() {
        LOGGER.info("Navigating to {}", TARGET_URL);
        driver.get(TARGET_URL);
    }

    @After
    public synchronized void tearDown() {
        if (driver != null) {
            LOGGER.info("Quitting WebDriver");
            try {
                driver.quit();
            } catch (Exception e) {
                LOGGER.error("Failed to quit WebDriver", e);
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
        JsonNode conditionNode = CONFIG_JSON.path("networkConditions").path(connectionParam.toUpperCase());

        if (conditionNode.isMissingNode()) {
            conditionNode = CONFIG_JSON.path("networkConditions").path(DEFAULT_CONNECTION_TYPE);
        }

        conditions.setNetworkConditions(
                ConnectionType.valueOf(conditionNode.path("connectionType").asText("")),
                conditionNode.path("latency").asInt(),
                conditionNode.path("downloadThroughput").asInt(),
                conditionNode.path("uploadThroughput").asInt(),
                conditionNode.path("packetLoss").asDouble()
        );

        LOGGER.debug("Network conditions determined for {}: Latency = {} ms, Download = {} bps, Upload = {} bps, Packet Loss = {}%",
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