package org.example.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Base class for all page objects. Provides common functionality for interacting with web elements.
 */
public abstract class BasePage {
    protected final WebDriver driver;
    protected final Wait<WebDriver> wait;
    protected final Map<By, WebElement> elementCache;
    protected static final Logger logger = LoggerFactory.getLogger(BasePage.class);

    private static final int DEFAULT_TIMEOUT_SECONDS = 10;
    private static final int DEFAULT_POLLING_INTERVAL_MILLIS = 500;

    /**
     * Constructor to initialize the BasePage with a WebDriver instance.
     *
     * @param driver WebDriver instance to interact with the web elements.
     */
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = createFluentWait();
        this.elementCache = new ConcurrentHashMap<>();
    }

    private FluentWait<WebDriver> createFluentWait() {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                .pollingEvery(Duration.ofMillis(DEFAULT_POLLING_INTERVAL_MILLIS))
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
    }

    /**
     * Waits for the specified element to satisfy a condition.
     *
     * @param locator   Locator of the web element.
     * @param condition Condition to be satisfied.
     * @param <V>       Type of the expected condition's return value.
     * @return The value returned by the condition.
     * @throws TimeoutException if the condition is not met within the timeout period.
     */
    public <V> V waitForElement(By locator, Function<? super WebDriver, V> condition) {
        try {
            return wait.until(condition);
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for element: {}", locator);
            throw e;
        }
    }

    /**
     * Waits for the specified element to be visible.
     *
     * @param locator Locator of the web element.
     * @throws TimeoutException if the element is not visible within the timeout period.
     */
    public void waitForElementToBeVisible(By locator) {
        waitForElement(locator, ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits for all specified elements to be visible.
     *
     * @param locators Array of locators for the web elements.
     * @throws TimeoutException if any element is not visible within the timeout period.
     */
    public void waitForElementsToBeVisible(By... locators) {
        Arrays.stream(locators).forEach(this::waitForElementToBeVisible);
    }

    /**
     * Waits for the specified element to be clickable.
     *
     * @param locator Locator of the web element.
     * @return The clickable WebElement.
     * @throws TimeoutException if the element is not clickable within the timeout period.
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
                .allMatch(CompletableFuture::join);
    }

    /**
     * Checks if a single element is displayed.
     *
     * @param locator Locator of the web element.
     * @return true if the element is displayed, false otherwise.
     */
    boolean isElementDisplayed(By locator) {
        try {
            return findElement(locator).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            logger.debug("Element not displayed: {}", locator);
            return false;
        }
    }

    /**
     * Finds an element using the element cache for improved performance.
     *
     * @param locator Locator of the web element.
     * @return The found WebElement.
     * @throws NoSuchElementException if the element is not found.
     */
    protected WebElement findElement(By locator) {
        return elementCache.computeIfAbsent(locator, key -> {
            try {
                return driver.findElement(key);
            } catch (NoSuchElementException e) {
                logger.error("Element not found: {}", key);
                throw e;
            }
        });
    }

    /**
     * Clears the element cache.
     */
    protected void clearElementCache() {
        elementCache.clear();
    }
}