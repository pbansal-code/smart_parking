package com.smartparking.tests;

import com.smartparking.base.BaseTest;
import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Test class for Booking functionality
 */
public class BookingTests extends BaseTest {

    // Locators
    private final By slotGrid = By.cssSelector(".slot-grid");
    private final By slotItems = By.cssSelector(".slot-item");
    private final By dateInput = By.id("date");
    private final By timeInput = By.id("time");
    private final By confirmButton = By.xpath("//button[text()='Confirm Booking']");
    private final By backLink = By.xpath("//a[@class='back-link']");
    private final By toastMessage = By.cssSelector(".toast .toast-message");
    private final By logoutButton = By.xpath("//button[text()='Logout']");

    @BeforeMethod
    public void setupLoggedInUser() {
        // Register and login a user first
        registerAndLoginUser();
    }

    /**
     * Test booking page loads when user is logged in
     */
    @Test(priority = 1, description = "Test booking page loads when logged in")
    public void testBookingPageLoadsWhenLoggedIn() {
        navigateToBook();
        
        waitFor(500);
        
        // Verify booking page is displayed
        Assert.assertTrue(driver.getCurrentUrl().contains("book.html"),
            "Booking page should be accessible when logged in");
        
        // Verify booking form is visible
        WebElement bookingForm = driver.findElement(By.cssSelector(".booking-form"));
        Assert.assertTrue(bookingForm.isDisplayed(),
            "Booking form should be visible");
    }

    /**
     * Test booking page redirects to login when not logged in
     */
    @Test(priority = 2, description = "Test booking page redirects to login when not logged in")
    public void testBookingPageRedirectsToLoginWhenNotLoggedIn() {
        // Clear localStorage to simulate logged out state
        clearLocalStorage();
        
        navigateToBook();
        
        waitFor(500);
        
        // Verify redirected to login page
        Assert.assertTrue(driver.getCurrentUrl().contains("login.html"),
            "Should redirect to login page when not logged in");
    }

    /**
     * Test slot grid is displayed on booking page
     */
    @Test(priority = 3, description = "Test slot grid is displayed")
    public void testSlotGridDisplayed() {
        navigateToBook();
        
        waitFor(500);
        
        // Verify slot grid is displayed
        WebElement grid = driver.findElement(slotGrid);
        Assert.assertTrue(grid.isDisplayed(),
            "Slot grid should be visible");
        
        // Verify we have 9 slots
        List<WebElement> slots = driver.findElements(slotItems);
        Assert.assertEquals(slots.size(), 9,
            "Should have 9 parking slots in grid");
    }

    /**
     * Test selecting an available slot
     */
    @Test(priority = 4, description = "Test selecting available slot")
    public void testSelectAvailableSlot() {
        navigateToBook();
        
        waitFor(500);
        
        // Click on an available slot (A1)
        WebElement slotA1 = driver.findElement(By.xpath("//div[@class='slot-item available' and text()='A1']"));
        slotA1.click();
        
        waitFor(500);
        
        // Verify slot is selected
        Assert.assertTrue(slotA1.getAttribute("class").contains("selected"),
            "Selected slot should have 'selected' class");
        
        // Verify toast message
        Assert.assertTrue(isToastDisplayed(), "Toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.contains("A1") || toastText.contains("selected"),
            "Toast should confirm slot selection");
    }

    /**
     * Test selecting an occupied slot shows error
     */
    @Test(priority = 5, description = "Test selecting occupied slot shows error")
    public void testSelectOccupiedSlot() {
        navigateToBook();
        
        waitFor(500);
        
        // Click on an occupied slot (B1)
        WebElement slotB1 = driver.findElement(By.xpath("//div[@class='slot-item occupied' and text()='B1']"));
        slotB1.click();
        
        waitFor(500);
        
        // Verify error toast is shown
        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.toLowerCase().contains("occupied"),
            "Toast should mention slot is occupied");
        
        // Verify slot is not selected
        Assert.assertFalse(slotB1.getAttribute("class").contains("selected"),
            "Occupied slot should not be selected");
    }

    /**
     * Test confirming booking with all fields filled
     */
    @Test(priority = 6, description = "Test successful booking with all fields")
    public void testSuccessfulBooking() {
        navigateToBook();
        
        waitFor(500);
        
        // Select a slot
        WebElement slotA1 = driver.findElement(By.xpath("//div[@class='slot-item available' and text()='A1']"));
        slotA1.click();
        
        waitFor(300);
        
        // Fill date (tomorrow)
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        driver.findElement(dateInput).sendKeys(tomorrow.format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        // Fill time
        driver.findElement(timeInput).sendKeys("10:00");
        
        // Click confirm booking
        driver.findElement(confirmButton).click();
        
        waitFor(2000);
        
        // Verify redirected to home page
        Assert.assertTrue(driver.getCurrentUrl().contains("home.html"),
            "Should redirect to home page after successful booking");
        
        // Verify success toast was shown
        String lastBooking = getLocalStorageItem("lastBooking");
        Assert.assertNotNull(lastBooking,
            "Booking should be stored in localStorage");
    }

    /**
     * Test booking without selecting a slot
     */
    @Test(priority = 7, description = "Test booking without selecting slot")
    public void testBookingWithoutSlot() {
        navigateToBook();
        
        waitFor(500);
        
        // Fill date and time but no slot
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        driver.findElement(dateInput).sendKeys(tomorrow.format(DateTimeFormatter.ISO_LOCAL_DATE));
        driver.findElement(timeInput).sendKeys("10:00");
        
        // Click confirm booking
        driver.findElement(confirmButton).click();
        
        waitFor(500);
        
        // Verify error toast is shown
        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.toLowerCase().contains("slot"),
            "Toast should mention slot is required");
    }

    /**
     * Test booking without selecting a date
     */
    @Test(priority = 8, description = "Test booking without selecting date")
    public void testBookingWithoutDate() {
        navigateToBook();
        
        waitFor(500);
        
        // Select a slot
        WebElement slotA1 = driver.findElement(By.xpath("//div[@class='slot-item available' and text()='A1']"));
        slotA1.click();
        
        waitFor(300);
        
        // Fill time but no date
        driver.findElement(timeInput).sendKeys("10:00");
        
        // Click confirm booking
        driver.findElement(confirmButton).click();
        
        waitFor(500);
        
        // Verify error toast is shown
        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.toLowerCase().contains("date"),
            "Toast should mention date is required");
    }

    /**
     * Test booking without selecting a time
     */
    @Test(priority = 9, description = "Test booking without selecting time")
    public void testBookingWithoutTime() {
        navigateToBook();
        
        waitFor(500);
        
        // Select a slot
        WebElement slotA1 = driver.findElement(By.xpath("//div[@class='slot-item available' and text()='A1']"));
        slotA1.click();
        
        waitFor(300);
        
        // Fill date but no time
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        driver.findElement(dateInput).sendKeys(tomorrow.format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        // Click confirm booking
        driver.findElement(confirmButton).click();
        
        waitFor(500);
        
        // Verify error toast is shown
        Assert.assertTrue(isToastDisplayed(), "Error toast should be displayed");
        String toastText = driver.findElement(toastMessage).getText();
        Assert.assertTrue(toastText.toLowerCase().contains("time"),
            "Toast should mention time is required");
    }

    /**
     * Test back link navigates to home page
     */
    @Test(priority = 10, description = "Test back link navigation")
    public void testBackLinkNavigation() {
        navigateToBook();
        
        waitFor(500);
        
        // Click back link
        driver.findElement(backLink).click();
        
        waitFor(500);
        
        // Verify redirected to home page
        Assert.assertTrue(driver.getCurrentUrl().contains("home.html"),
            "Should navigate to home page via back link");
    }

    /**
     * Test logout from booking page
     */
    @Test(priority = 11, description = "Test logout from booking page")
    public void testLogoutFromBookingPage() {
        navigateToBook();
        
        waitFor(500);
        
        // Click logout button in navbar
        driver.findElement(logoutButton).click();
        
        waitFor(1500);
        
        // Verify redirected to login page
        Assert.assertTrue(driver.getCurrentUrl().contains("login.html"),
            "Should redirect to login page after logout");
        
        // Verify loggedIn is cleared
        String loggedIn = getLocalStorageItem("loggedIn");
        Assert.assertNull(loggedIn,
            "loggedIn should be cleared from localStorage");
    }

    /**
     * Test slot selection updates hidden input
     */
    @Test(priority = 12, description = "Test hidden input updates on slot selection")
    public void testHiddenInputUpdates() {
        navigateToBook();
        
        waitFor(500);
        
        // Get the hidden slot input
        WebElement slotInput = driver.findElement(By.id("slot"));
        
        // Select a slot
        WebElement slotA2 = driver.findElement(By.xpath("//div[@class='slot-item available' and text()='A2']"));
        slotA2.click();
        
        waitFor(300);
        
        // Verify hidden input value is updated
        Assert.assertEquals(slotInput.getAttribute("value"), "A2",
            "Hidden input should contain selected slot value");
    }

    /**
     * Test multiple slot selection (only one should be selected at a time)
     */
    @Test(priority = 13, description = "Test only one slot can be selected in booking")
    public void testOnlyOneSlotSelectedInBooking() {
        navigateToBook();
        
        waitFor(500);
        
        // Click on first available slot (A1)
        WebElement slotA1 = driver.findElement(By.xpath("//div[@class='slot-item available' and text()='A1']"));
        slotA1.click();
        
        waitFor(300);
        
        // Verify A1 is selected
        Assert.assertTrue(slotA1.getAttribute("class").contains("selected"),
            "First selected slot should have 'selected' class");
        
        // Click on second available slot (A2)
        WebElement slotA2 = driver.findElement(By.xpath("//div[@class='slot-item available' and text()='A2']"));
        slotA2.click();
        
        waitFor(300);
        
        // Verify A1 is no longer selected
        Assert.assertFalse(slotA1.getAttribute("class").contains("selected"),
            "First slot should not be selected after selecting another slot");
        
        // Verify A2 is now selected
        Assert.assertTrue(slotA2.getAttribute("class").contains("selected"),
            "Second selected slot should have 'selected' class");
    }

    /**
     * Test date input field is present and functional
     */
    @Test(priority = 14, description = "Test date input field")
    public void testDateInputField() {
        navigateToBook();
        
        waitFor(500);
        
        // Verify date input is present
        WebElement dateField = driver.findElement(dateInput);
        Assert.assertTrue(dateField.isDisplayed(),
            "Date input should be visible");
        
        // Verify we can enter a date
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        dateField.sendKeys(tomorrow.format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        // Verify date was entered
        Assert.assertFalse(dateField.getAttribute("value").isEmpty(),
            "Date should be entered in the field");
    }

    /**
     * Test time input field is present and functional
     */
    @Test(priority = 15, description = "Test time input field")
    public void testTimeInputField() {
        navigateToBook();
        
        waitFor(500);
        
        // Verify time input is present
        WebElement timeField = driver.findElement(timeInput);
        Assert.assertTrue(timeField.isDisplayed(),
            "Time input should be visible");
        
        // Verify we can enter a time
        timeField.sendKeys("14:30");
        
        // Verify time was entered
        Assert.assertFalse(timeField.getAttribute("value").isEmpty(),
            "Time should be entered in the field");
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

