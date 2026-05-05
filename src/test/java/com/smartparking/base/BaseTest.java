package com.smartparking.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.*;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all Selenium tests
 * Provides WebDriver setup and common utilities
 */
public class BaseTest {

    protected WebDriver driver;
    protected String baseUrl;

    // Test data constants
    protected static final String VALID_EMAIL = "testuser@example.com";
    protected static final String VALID_PASSWORD = "Test@1234";
    protected static final String VALID_NAME = "Test User";
    protected static final String VALID_VEHICLE = "ABC123";

    @BeforeMethod
    public void setUp() {
        // Setup Chrome WebDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Configure Chrome options for testing
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        
        // Set download directory for any file downloads
        String downloadPath = System.getProperty("user.dir") + "/downloads";
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadPath);
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        
        // Set implicit wait
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        // Set page load timeout
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        
        // Get the absolute path to the HTML files
        String projectPath = System.getProperty("user.dir");
        baseUrl = "file://" + projectPath + "/";
    }

    /**
     * Navigate to a specific HTML page
     */
    protected void navigateTo(String pageName) {
        driver.get(baseUrl + pageName);
    }

    /**
     * Navigate to login page
     */
    protected void navigateToLogin() {
        navigateTo("login.html");
    }

    /**
     * Navigate to registration page
     */
    protected void navigateToRegister() {
        navigateTo("register.html");
    }

    /**
     * Navigate to home page
     */
    protected void navigateToHome() {
        navigateTo("home.html");
    }

    /**
     * Navigate to booking page
     */
    protected void navigateToBook() {
        navigateTo("book.html");
    }

    /**
     * Clear localStorage
     */
    protected void clearLocalStorage() {
        ((JavascriptExecutor) driver).executeScript("localStorage.clear();");
    }

    /**
     * Execute JavaScript to set localStorage item
     */
    protected void setLocalStorageItem(String key, String value) {
        ((JavascriptExecutor) driver).executeScript(
            "localStorage.setItem(arguments[0], arguments[1]);", key, value);
    }

    /**
     * Get localStorage item
     */
    protected String getLocalStorageItem(String key) {
        return (String) ((JavascriptExecutor) driver).executeScript(
            "return localStorage.getItem(arguments[0]);", key);
    }

    /**
     * Wait for a specific time (use sparingly)
     */
    protected void waitFor(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if element exists in DOM
     */
    protected boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Get toast message text
     */
    protected String getToastMessage() {
        WebElement toast = driver.findElement(By.cssSelector(".toast"));
        return toast.getText();
    }

    /**
     * Check if toast is displayed
     */
    protected boolean isToastDisplayed() {
        try {
            WebElement toast = driver.findElement(By.cssSelector(".toast"));
            return toast.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

