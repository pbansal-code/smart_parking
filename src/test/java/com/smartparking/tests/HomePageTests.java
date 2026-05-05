package com.smartparking.tests;

import com.smartparking.base.BaseTest;
import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * Test class for Home Page functionality
 */
public class HomePageTests extends BaseTest {

    // Locators
    private final By logoutButton = By.xpath("//button[text()='Logout']");
    private final By bookButton = By.xpath("//button[text()='Book Parking Slot']");
    private final By parkingSlots = By.cssSelector(".slot");
    private final By toastMessage = By.cssSelector(".toast .toast-message");

    @BeforeMethod
    public void setupLoggedInUser() {
        // Register and login a user first
        registerAndLoginUser();
    }

    /**
     * Test home page loads successfully when user is logged in
     */
    @Test(priority = 1, description = "Test home page loads when user is logged in")
    public void testHomePageLoadsWhenLoggedIn() {
        navigateToHome();
        
        waitFor(500);
        
        // Verify home page is displayed
        Assert.assertTrue(driver.getCurrentUrl().contains("home.html"),
            "Home page should be accessible when logged in");
        
        // Verify dashboard is visible
        WebElement dashboard = driver.findElement(By.cssSelector(".dashboard"));
        Assert.assertTrue(dashboard.isDisplayed(),
            "Dashboard should be visible");
    }

    /**
     * Test home page redirects to login when user is not logged in
     */
    @Test(priority = 2, description = "Test home page redirects to login when not logged in")
    public void testHomePageRedirectsToLoginWhenNotLoggedIn() {
        // Clear localStorage to simulate logged out state
        clearLocalStorage();
        
        navigateToHome();
        
        waitFor(500);
        
        // Verify redirected to login page
        Assert.assertTrue(driver.getCurrentUrl().contains("login.html"),
            "Should redirect to login page when not logged in");
    }

    /**
     * Test logout functionality
     */
    @Test(priority = 3, description = "Test logout functionality")
    public void testLogout() {
        navigateToHome();
        
        waitFor(500);
        
        // Click logout button
        driver.findElement(logoutButton).click();
        
        waitFor(1500);
        
        // Verify redirected to login page
        Assert.assertTrue(driver.getCurrentUrl().contains("login.html"),
            "Should redirect to login page after logout");
        
        // Verify loggedIn is cleared from localStorage
        String loggedIn = getLocalStorageItem("loggedIn");
        Assert.assertNull(loggedIn,
            "loggedIn should be cleared from localStorage after logout");
    }

    /**
     * Test navigation to booking page from home
     */
    @Test(priority = 4, description = "Test navigation to booking page")
    public void testNavigateToBookingPage() {
        navigateToHome();
        
        waitFor(500);
        
        // Click book parking slot button
        driver.findElement(bookButton).click();
        
        waitFor(500);
        
        // Verify redirected to booking page
        Assert.assertTrue(driver.getCurrentUrl().contains("book.html"),
            "Should navigate to booking page");
    }

    /**
     * Test available parking slots are displayed
     */
    @Test(priority = 5, description = "Test available parking slots are displayed")
    public void testParkingSlotsDisplayed() {
        navigateToHome();
        
        waitFor(500);
        
        // Find all parking slots
        java.util.List<WebElement> slots = driver.findElements(parkingSlots);
        
        Assert.assertTrue(slots.size() > 0,
            "Parking slots should be displayed");
        
        // Verify we have 9 slots (A1-A3, B1-B3, C1-C3)
        Assert.assertEquals(slots.size(), 9,
            "Should have 9 parking slots");
    }

    /**
     * Test clicking on available slot shows selection
     */
    @Test(priority = 6, description = "Test clicking available slot")
    public void testClickAvailableSlot() {
        navigateToHome();
        
        waitFor(500);
        
        // Click on an available slot (A1)
        WebElement slotA1 = driver.findElement(By.xpath("//div[@class='slot available' and contains(text(),'A1')]"));
        slotA1.click();
        
        waitFor(500);
        
        // Verify toast message is shown
        Assert.assertTrue(isToastDisplayed(), "Toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.contains("A1") || toastText.contains("Selected"),
            "Toast should confirm slot selection");
    }

    /**
     * Test clicking on occupied slot does not select it
     */
    @Test(priority = 7, description = "Test clicking occupied slot")
    public void testClickOccupiedSlot() {
        navigateToHome();
        
        waitFor(500);
        
        // Click on an occupied slot (B1)
        WebElement slotB1 = driver.findElement(By.xpath("//div[@class='slot occupied' and contains(text(),'B1')]"));
        slotB1.click();
        
        waitFor(500);
        
        // The slot should not be selected (it will show a toast about selection attempt)
        // Verify slot is not marked as selected
        Assert.assertFalse(slotB1.getAttribute("class").contains("selected"),
            "Occupied slot should not be selectable");
    }

    /**
     * Test slot selection is visually indicated
     */
    @Test(priority = 8, description = "Test slot selection visual indicator")
    public void testSlotSelectionVisualIndicator() {
        navigateToHome();
        
        waitFor(500);
        
        // Click on an available slot (A2)
        WebElement slotA2 = driver.findElement(By.xpath("//div[@class='slot available' and contains(text(),'A2')]"));
        slotA2.click();
        
        waitFor(500);
        
        // Verify the slot has selected class
        Assert.assertTrue(slotA2.getAttribute("class").contains("selected"),
            "Selected slot should have 'selected' class");
    }

    /**
     * Test multiple slot selection (only one should be selected at a time)
     */
    @Test(priority = 9, description = "Test only one slot can be selected at a time")
    public void testOnlyOneSlotSelectedAtATime() {
        navigateToHome();
        
        waitFor(500);
        
        // Click on first available slot (A1)
        WebElement slotA1 = driver.findElement(By.xpath("//div[@class='slot available' and contains(text(),'A1')]"));
        slotA1.click();
        
        waitFor(500);
        
        // Verify A1 is selected
        Assert.assertTrue(slotA1.getAttribute("class").contains("selected"),
            "First selected slot should have 'selected' class");
        
        // Click on second available slot (A2)
        WebElement slotA2 = driver.findElement(By.xpath("//div[@class='slot available' and contains(text(),'A2')]"));
        slotA2.click();
        
        waitFor(500);
        
        // Verify A1 is no longer selected
        Assert.assertFalse(slotA1.getAttribute("class").contains("selected"),
            "First slot should not be selected after selecting another slot");
        
        // Verify A2 is now selected
        Assert.assertTrue(slotA2.getAttribute("class").contains("selected"),
            "Second selected slot should have 'selected' class");
    }

    /**
     * Test navbar displays correctly
     */
    @Test(priority = 10, description = "Test navbar elements")
    public void testNavbarElements() {
        navigateToHome();
        
        waitFor(500);
        
        // Verify navbar is displayed
        WebElement navbar = driver.findElement(By.cssSelector(".navbar"));
        Assert.assertTrue(navbar.isDisplayed(),
            "Navbar should be visible");
        
        // Verify app name is shown
        Assert.assertTrue(navbar.getText().contains("Smart Parking"),
            "App name should be displayed in navbar");
        
        // Verify logout button is present
        WebElement logoutBtn = driver.findElement(logoutButton);
        Assert.assertTrue(logoutBtn.isDisplayed(),
            "Logout button should be visible");
    }

    /**
     * Helper method to register and login a test user
     */
    private void registerAndLoginUser() {
        // Navigate to registration
        navigateToRegister();
        
        // Fill registration form
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
        
        // Now login
        navigateToLogin();
        
        By loginEmailInput = By.id("loginEmail");
        By loginPasswordInput = By.id("loginPassword");
        By loginButton = By.xpath("//button[text()='Login']");
        
        driver.findElement(loginEmailInput).sendKeys(VALID_EMAIL);
        driver.findElement(loginPasswordInput).sendKeys(VALID_PASSWORD);
        
        driver.findElement(loginButton).click();
        
        waitFor(2000);
    }
}

