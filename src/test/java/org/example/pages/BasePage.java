package org.example.pages;

import org.example.steps.homesteps;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.time.Duration;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected final Map<By, WebElement> elementCache = new HashMap<>();
    protected static final Logger logger = LoggerFactory.getLogger(homesteps.class);

    // Constructor
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Wait for element to be visible
    private void waitForElementToBeVisible(By locator) {
    wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void waitForElementsToBeVisible(By... locators) {
        for (By locator : locators) {
            try { waitForElementToBeVisible(locator); }
            catch (Exception e)
            {
                logger.error("An error occurred while waiting for elements to be visible", e);
                throw new RuntimeException("An error occurred while waiting for elements to be visible", e);
            }
        }
    }

    // Wait for element to be clickable
    protected void waitForElementToBeClickable(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    protected boolean areElementsDisplayed(By... locators) {
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