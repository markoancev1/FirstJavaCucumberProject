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
    By ulNavbar = By.id("menu-primary-items");
    By imgPicture = By.cssSelector("img.wp-image-91");

    // Methods
    public void checkImageVisibility() {
        waitForElementsToBeVisible(imgPicture, ulNavbar);
        boolean allDisplayed = areElementsDisplayed(imgPicture, ulNavbar);
        Assert.assertTrue(allDisplayed);
    }
}
