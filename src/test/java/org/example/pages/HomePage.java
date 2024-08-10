package org.example.pages;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage{
    // Constructor
    public HomePage(WebDriver driver) {
        super(driver);
    }

    // Locators
    private static final By NAVBAR = By.id("menu-primary-items");
    private static final By MAIN_IMAGE = By.cssSelector("img.wp-image-91");
    private static final By NAVBAR_ITEMS = By.cssSelector("#menu-primary-items li");

    // Methods
    public void checkImageVisibility() {
        waitForElementsToBeVisible(MAIN_IMAGE, NAVBAR);
        boolean allDisplayed = areElementsDisplayed(MAIN_IMAGE, NAVBAR);
        Assert.assertTrue(allDisplayed);
    }
}