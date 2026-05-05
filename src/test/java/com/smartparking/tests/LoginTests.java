package com.smartparking.tests;

import com.smartparking.base.BaseTest;
import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * Test class for Login functionality
 */
public class LoginTests extends BaseTest {

    // Locators
    private final By emailInput = By.id("loginEmail");
    private final By passwordInput = By.id("loginPassword");
    private final By loginButton = By.xpath("//button[text()='Login']");
    private final By registerLink = By.xpath("//a[contains(text(),'New user')]");
    private final By toastMessage = By.cssSelector(".toast .toast-message");

    @BeforeMethod
    public void navigateToLoginPage() {
        navigateToLogin();
    }

    /**
     * Test successful login with valid credentials
     * Pre-condition: User must be registered first
     */
    @Test(priority = 1, description = "Test successful login with valid credentials")
    public void testSuccessfulLogin() {
        // First register a user
        navigateToRegister();
        registerTestUser();
        
        // Navigate to login
        navigateToLogin();
        
        // Enter valid credentials
        driver.findElement(emailInput).sendKeys(VALID_EMAIL);
        driver.findElement(passwordInput).sendKeys(VALID_PASSWORD);
        
        // Click login button
        driver.findElement(loginButton).click();
        
        // Wait for redirect
        waitFor(2000);
        
        // Verify redirected to home page
        Assert.assertTrue(driver.getCurrentUrl().contains("home.html"),
            "Should redirect to home page after successful login");
        
        // Verify user is logged in
        String loggedIn = getLocalStorageItem("loggedIn");
        Assert.assertEquals(loggedIn, "true",
            "User should be marked as logged in");
    }

    /**
     * Test login with empty email field
     */
    @Test(priority = 2, description = "Test login with empty email")
    public void testLoginWithEmptyEmail() {
        driver.findElement(passwordInput).sendKeys(VALID_PASSWORD);
        
        driver.findElement(loginButton).click();
        
        waitFor(500);
        
        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.contains("email"),
            "Toast should mention email is required");
    }

    /**
     * Test login with empty password field
     */
    @Test(priority = 3, description = "Test login with empty password")
    public void testLoginWithEmptyPassword() {
        driver.findElement(emailInput).sendKeys(VALID_EMAIL);
        
        driver.findElement(loginButton).click();
        
        waitFor(500);
        
        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.contains("password"),
            "Toast should mention password is required");
    }

    /**
     * Test login with both fields empty
     */
    @Test(priority = 4, description = "Test login with empty fields")
    public void testLoginWithEmptyFields() {
        driver.findElement(loginButton).click();
        
        waitFor(500);
        
        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.contains("email"),
            "Toast should mention email is required");
    }

    /**
     * Test login with invalid credentials (wrong password)
     */
    @Test(priority = 5, description = "Test login with wrong password")
    public void testLoginWithWrongPassword() {
        // First register a user
        navigateToRegister();
        registerTestUser();
        
        // Navigate to login
        navigateToLogin();
        
        // Enter wrong password
        driver.findElement(emailInput).sendKeys(VALID_EMAIL);
        driver.findElement(passwordInput).sendKeys("WrongPassword123");
        
        driver.findElement(loginButton).click();
        
        waitFor(1500);
        
        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.contains("Invalid") || toastText.contains("incorrect"),
            "Toast should mention invalid credentials");
        
        // Verify still on login page
        Assert.assertTrue(driver.getCurrentUrl().contains("login.html"),
            "Should stay on login page after failed login");
    }

    /**
     * Test login with unregistered email
     */
    @Test(priority = 6, description = "Test login with unregistered email")
    public void testLoginWithUnregisteredEmail() {
        driver.findElement(emailInput).sendKeys("notregistered@example.com");
        driver.findElement(passwordInput).sendKeys(VALID_PASSWORD);
        
        driver.findElement(loginButton).click();
        
        waitFor(1500);
        
        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.contains("Invalid") || toastText.contains("incorrect"),
            "Toast should mention invalid credentials");
    }

    /**
     * Test navigation to registration page from login
     */
    @Test(priority = 7, description = "Test navigation to registration from login")
    public void testNavigateToRegistration() {
        driver.findElement(registerLink).click();
        
        waitFor(500);
        
        Assert.assertTrue(driver.getCurrentUrl().contains("register.html"),
            "Should navigate to registration page");
    }

    /**
     * Test login with form submission using Enter key
     */
    @Test(priority = 8, description = "Test login with Enter key submission")
    public void testLoginWithEnterKey() {
        // First register a user
        navigateToRegister();
        registerTestUser();
        
        // Navigate to login
        navigateToLogin();
        
        // Enter credentials and press Enter
        driver.findElement(emailInput).sendKeys(VALID_EMAIL);
        driver.findElement(passwordInput).sendKeys(VALID_PASSWORD + "\n");
        
        // Wait for redirect
        waitFor(2000);
        
        // Verify redirected to home page
        Assert.assertTrue(driver.getCurrentUrl().contains("home.html"),
            "Should redirect to home page when pressing Enter");
    }

    /**
     * Helper method to register a test user
     */
    private void registerTestUser() {
        By nameInput = By.id("name");
        By emailInput = By.id("email");
        By vehicleInput = By.id("vehicle");
        By passwordInput = By.id("password");
        By registerButton = By.xpath("//button[text()='Register Now']");
        
        driver.findElement(nameInput).sendKeys(VALID_NAME);
        driver.findElement(emailInput).sendKeys(VALID_EMAIL);
        driver.findElement(vehicleInput).sendKeys(VALID_VEHICLE);
        driver.findElement(passwordInput).sendKeys(VALID_PASSWORD);
        
        driver.findElement(registerButton).click();
        
        waitFor(2000);
    }
}

