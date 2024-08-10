package org.example.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Base class for all page objects. Provides common functionality for interacting with web elements.
 */
public abstract class BasePage {
    protected final WebDriver driver;
    protected final Wait<WebDriver> wait;
    protected final Map<By, WebElement> elementCache;
    protected static final Logger logger = LoggerFactory.getLogger(BasePage.class);

    private static final int DEFAULT_TIMEOUT = 10;
    private static final int DEFAULT_POLLING_INTERVAL = 500;

    /**
     * Constructor to initialize the BasePage with a WebDriver instance.
     *
     * @param driver WebDriver instance to interact with the web elements.
     */
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT))
                .pollingEvery(Duration.ofMillis(DEFAULT_POLLING_INTERVAL))
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
        this.elementCache = new HashMap<>();
    }

    /**
     * Waits for the specified element to satisfy a condition.
     *
     * @param locator   Locator of the web element.
     * @param condition Condition to be satisfied.
     * @param <V>       Type of the expected condition's return value.
     * @return The value returned by the condition.
     */
    public <V> V waitForElement(By locator, Function<? super WebDriver, V> condition) {
        return wait.until(condition);
    }

    /**
     * Waits for the specified element to be visible.
     *
     * @param locator Locator of the web element.
     */
    public void waitForElementToBeVisible(By locator) {
        waitForElement(locator, ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits for all specified elements to be visible.
     *
     * @param locators Array of locators for the web elements.
     */
    public void waitForElementsToBeVisible(By... locators) {
        Arrays.stream(locators).forEach(this::waitForElementToBeVisible);
    }

    /**
     * Waits for the specified element to be clickable.
     *
     * @param locator Locator of the web element.
     * @return The clickable WebElement.
     */
    public WebElement waitForElementToBeClickable(By locator) {
        return waitForElement(locator, ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Checks if all specified elements are displayed.
     *
     * @param locators Array of locators for the web elements.
     * @return true if all elements are displayed, false otherwise.
     */
    public boolean areElementsDisplayed(By... locators) {
        return Arrays.stream(locators)
                .map(locator -> CompletableFuture.supplyAsync(() -> isElementDisplayed(locator)))
                .toList()
                .stream()
                .allMatch(CompletableFuture::join);
    }

    /**
     * Checks if a single element is displayed.
     *
     * @param locator Locator of the web element.
     * @return true if the element is displayed, false otherwise.
     */
    private boolean isElementDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    /**
     * Finds an element using the element cache for improved performance.
     *
     * @param locator Locator of the web element.
     * @return The found WebElement.
     */
    protected WebElement findElement(By locator) {
        return elementCache.computeIfAbsent(locator, driver::findElement);
    }

    /**
     * Clears the element cache.
     */
    protected void clearElementCache() {
        elementCache.clear();
    }
}