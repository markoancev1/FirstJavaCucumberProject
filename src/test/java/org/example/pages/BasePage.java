package org.example.pages;

import org.example.steps.homesteps;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Base class for all page classes. Provides common functionality for interacting with web elements.
 */
public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected final Map<By, WebElement> elementCache = new HashMap<>();
    protected static final Logger logger = LoggerFactory.getLogger(homesteps.class);

    /**
     * Constructor to initialize the BasePage with a WebDriver instance.
     *
     * @param driver WebDriver instance to interact with the web elements.
     */
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Waits for the specified element to be visible.
     *
     * @param locator Locator of the web element.
     */
    public void waitForElementToBeVisible(By locator) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (Exception e) {
            logger.error("An error occurred while waiting for element to be visible: {}", locator, e);
            throw new RuntimeException("An error occurred while waiting for element to be visible", e);
        }
    }

    /**
     * Waits for all specified elements to be visible.
     *
     * @param locators Array of locators for the web elements.
     */
    public void waitForElementsToBeVisible(By... locators) {
        for (By locator : locators) {
            waitForElementToBeVisible(locator);
        }
    }

    /**
     * Waits for the specified element to be clickable.
     *
     * @param element Web element to wait for.
     */
    public void waitForElementToBeClickable(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Checks if all specified elements are displayed.
     *
     * @param locators Array of locators for the web elements.
     * @return true if all elements are displayed, false otherwise.
     */
    public boolean areElementsDisplayed(By... locators) {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (By locator : locators) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    WebElement element = driver.findElement(locator);
                    return element.isDisplayed();
                } catch (NoSuchElementException e) {
                    return false;
                }
            }));
        }

        try {
            for (CompletableFuture<Boolean> future : futures) {
                if (!future.get()) {
                    return false;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("An error occurred while checking for elements to be displayed", e);
            return false;
        }

        return true;
    }
}