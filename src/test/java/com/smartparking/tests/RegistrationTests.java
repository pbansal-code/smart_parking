package com.smartparking.tests;

import com.smartparking.base.BaseTest;
import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;

/**
 * Test class for Registration functionality
 */
public class RegistrationTests extends BaseTest {

    // Locators
    private final By nameInput = By.id("name");
    private final By emailInput = By.id("email");
    private final By vehicleInput = By.id("vehicle");
    private final By passwordInput = By.id("password");
    private final By registerButton = By.xpath("//button[text()='Register Now']");
    private final By loginLink = By.xpath("//a[contains(text(),'Already registered')]");
    private final By toastMessage = By.cssSelector(".toast .toast-message");

    @BeforeMethod
    public void navigateToRegistration() {
        navigateToRegister();
    }

    /**
     * Test successful user registration with valid data
     */
    @Test(priority = 1, description = "Test successful registration with valid data")
    public void testSuccessfulRegistration() {
        // Fill in registration form
        driver.findElement(nameInput).sendKeys(VALID_NAME);
        driver.findElement(emailInput).sendKeys(VALID_EMAIL);
        driver.findElement(vehicleInput).sendKeys(VALID_VEHICLE);
        driver.findElement(passwordInput).sendKeys(VALID_PASSWORD);

        // Click register button
        driver.findElement(registerButton).click();

        // Wait for redirect
        waitFor(2000);

        // Verify redirected to login page
        Assert.assertTrue(driver.getCurrentUrl().contains("login.html"),
            "Should redirect to login page after successful registration");

        // Verify success message was shown
        String userEmail = getLocalStorageItem("userEmail");
        Assert.assertEquals(userEmail, VALID_EMAIL,
            "User email should be stored in localStorage");
    }

    /**
     * Test registration with empty name field
     */
    @Test(priority = 2, description = "Test registration with empty name")
    public void testRegistrationWithEmptyName() {
        driver.findElement(emailInput).sendKeys(VALID_EMAIL);
        driver.findElement(vehicleInput).sendKeys(VALID_VEHICLE);
        driver.findElement(passwordInput).sendKeys(VALID_PASSWORD);

        driver.findElement(registerButton).click();

        waitFor(500);

        // Verify error toast is displayed
        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.contains("full name"),
            "Toast should mention full name is required");
    }

    /**
     * Test registration with empty email field
     */
    @Test(priority = 3, description = "Test registration with empty email")
    public void testRegistrationWithEmptyEmail() {
        driver.findElement(nameInput).sendKeys(VALID_NAME);
        driver.findElement(vehicleInput).sendKeys(VALID_VEHICLE);
        driver.findElement(passwordInput).sendKeys(VALID_PASSWORD);

        driver.findElement(registerButton).click();

        waitFor(500);

        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.contains("email"),
            "Toast should mention email is required");
    }

    /**
     * Test registration with empty vehicle field
     */
    @Test(priority = 4, description = "Test registration with empty vehicle number")
    public void testRegistrationWithEmptyVehicle() {
        driver.findElement(nameInput).sendKeys(VALID_NAME);
        driver.findElement(emailInput).sendKeys(VALID_EMAIL);
        driver.findElement(passwordInput).sendKeys(VALID_PASSWORD);

        driver.findElement(registerButton).click();

        waitFor(500);

        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.contains("vehicle"),
            "Toast should mention vehicle number is required");
    }

    /**
     * Test registration with empty password field
     */
    @Test(priority = 5, description = "Test registration with empty password")
    public void testRegistrationWithEmptyPassword() {
        driver.findElement(nameInput).sendKeys(VALID_NAME);
        driver.findElement(emailInput).sendKeys(VALID_EMAIL);
        driver.findElement(vehicleInput).sendKeys(VALID_VEHICLE);

        driver.findElement(registerButton).click();

        waitFor(500);

        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.contains("password"),
            "Toast should mention password is required");
    }

    /**
     * Test registration with invalid email format
     */
    @Test(priority = 6, description = "Test registration with invalid email format")
    public void testRegistrationWithInvalidEmail() {
        driver.findElement(nameInput).sendKeys(VALID_NAME);
        driver.findElement(emailInput).sendKeys("invalid-email");
        driver.findElement(vehicleInput).sendKeys(VALID_VEHICLE);
        driver.findElement(passwordInput).sendKeys(VALID_PASSWORD);

        driver.findElement(registerButton).click();

        waitFor(500);

        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.contains("valid email") || toastText.contains("valid email address"),
            "Toast should mention valid email is required");
    }

    /**
     * Test registration with short password (less than 8 characters)
     */
    @Test(priority = 7, description = "Test registration with short password")
    public void testRegistrationWithShortPassword() {
        driver.findElement(nameInput).sendKeys(VALID_NAME);
        driver.findElement(emailInput).sendKeys(VALID_EMAIL);
        driver.findElement(vehicleInput).sendKeys(VALID_VEHICLE);
        driver.findElement(passwordInput).sendKeys("Abc@12");

        driver.findElement(registerButton).click();

        waitFor(500);

        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.contains("8 characters") || toastText.contains("at least 8"),
            "Toast should mention password must be at least 8 characters");
    }

    /**
     * Test registration with password missing uppercase letter
     */
    @Test(priority = 8, description = "Test registration with password missing uppercase")
    public void testRegistrationWithPasswordNoUppercase() {
        driver.findElement(nameInput).sendKeys(VALID_NAME);
        driver.findElement(emailInput).sendKeys(VALID_EMAIL);
        driver.findElement(vehicleInput).sendKeys(VALID_VEHICLE);
        driver.findElement(passwordInput).sendKeys("test@1234");

        driver.findElement(registerButton).click();

        waitFor(500);

        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.toLowerCase().contains("uppercase"),
            "Toast should mention uppercase letter is required");
    }

    /**
     * Test registration with password missing lowercase letter
     */
    @Test(priority = 9, description = "Test registration with password missing lowercase")
    public void testRegistrationWithPasswordNoLowercase() {
        driver.findElement(nameInput).sendKeys(VALID_NAME);
        driver.findElement(emailInput).sendKeys(VALID_EMAIL);
        driver.findElement(vehicleInput).sendKeys(VALID_VEHICLE);
        driver.findElement(passwordInput).sendKeys("TEST@1234");

        driver.findElement(registerButton).click();

        waitFor(500);

        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.toLowerCase().contains("lowercase"),
            "Toast should mention lowercase letter is required");
    }

    /**
     * Test registration with password missing number
     */
    @Test(priority = 10, description = "Test registration with password missing number")
    public void testRegistrationWithPasswordNoNumber() {
        driver.findElement(nameInput).sendKeys(VALID_NAME);
        driver.findElement(emailInput).sendKeys(VALID_EMAIL);
        driver.findElement(vehicleInput).sendKeys(VALID_VEHICLE);
        driver.findElement(passwordInput).sendKeys("Test@Pass");

        driver.findElement(registerButton).click();

        waitFor(500);

        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.toLowerCase().contains("number"),
            "Toast should mention number is required");
    }

    /**
     * Test registration with password missing special character
     */
    @Test(priority = 11, description = "Test registration with password missing special char")
    public void testRegistrationWithPasswordNoSpecialChar() {
        driver.findElement(nameInput).sendKeys(VALID_NAME);
        driver.findElement(emailInput).sendKeys(VALID_EMAIL);
        driver.findElement(vehicleInput).sendKeys(VALID_VEHICLE);
        driver.findElement(passwordInput).sendKeys("TestPass123");

        driver.findElement(registerButton).click();

        waitFor(500);

        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.toLowerCase().contains("special"),
            "Toast should mention special character is required");
    }

    /**
     * Test password strength indicator updates in real-time
     */
    @Test(priority = 12, description = "Test password strength indicator")
    public void testPasswordStrengthIndicator() {
        // Enter password with some requirements met
        WebElement password = driver.findElement(passwordInput);
        password.sendKeys("Test");

        // Check if requirement indicators are visible
        By lengthReq = By.id("req-length");
        By uppercaseReq = By.id("req-uppercase");

        waitFor(300);

        // Verify password requirements section is displayed
        List<WebElement> requirements = driver.findElements(By.cssSelector(".requirement"));
        Assert.assertTrue(requirements.size() > 0,
            "Password requirements should be displayed");
    }

    /**
     * Test navigation to login page from registration
     */
    @Test(priority = 13, description = "Test navigation to login from registration")
    public void testNavigateToLogin() {
        driver.findElement(loginLink).click();

        waitFor(500);

        Assert.assertTrue(driver.getCurrentUrl().contains("login.html"),
            "Should navigate to login page");
    }
}

