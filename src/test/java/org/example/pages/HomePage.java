package org.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HomePage extends BasePage{
    // Constructor
    public HomePage(WebDriver driver) {
        super(driver);
    }

    // Locators
    By imgPicture = By.cssSelector("img.wp-image-91");

    // Methods
    public void checkImageVisibility() {
        System.out.println(driver);
        WebElement imgElement = driver.findElement(imgPicture);
        waitForElementToBeVisible(imgElement);
    }
}
