package org.example.pages;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.List;

/**
 * Represents the Home Page of the website.
 * This class contains locators and methods specific to the Home Page.
 */
public class HomePage extends BasePage {

    // Locators
    private static final By NAVBAR = By.id("menu-primary-items");
    private static final By MAIN_IMAGE = By.cssSelector("img.wp-image-91");
    private static final By NAVBAR_ITEMS = By.cssSelector("#menu-primary-items li");

    /**
     * Constructor for HomePage.
     *
     * @param driver WebDriver instance to interact with the web elements.
     */
    public HomePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Checks if the main image and navbar are visible on the page.
     *
     * @throws AssertionError if either the main image or navbar is not visible.
     */
    public void verifyMainElementsVisibility() {
        waitForElementsToBeVisible(MAIN_IMAGE, NAVBAR);
        Assert.assertTrue("Main image is not displayed", isElementDisplayed(MAIN_IMAGE));
        Assert.assertTrue("Navbar is not displayed", isElementDisplayed(NAVBAR));
    }

    /**
     * Gets the number of items in the navbar.
     *
     * @return The number of navbar items.
     */
    public int getNavbarItemCount() {
        List<WebElement> navbarItems = driver.findElements(NAVBAR_ITEMS);
        return navbarItems.size();
    }

    /**
     * Checks if a specific item is present in the navbar.
     *
     * @param itemText The text of the navbar item to search for.
     * @return true if the item is found, false otherwise.
     */
    public boolean isNavbarItemPresent(String itemText) {
        List<WebElement> navbarItems = driver.findElements(NAVBAR_ITEMS);
        return navbarItems.stream()
                .anyMatch(item -> item.getText().equalsIgnoreCase(itemText));
    }
}