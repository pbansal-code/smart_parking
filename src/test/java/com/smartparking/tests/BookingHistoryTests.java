package com.smartparking.tests;

import com.smartparking.base.BaseTest;
import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;

/**
 * Test class for Booking History functionality
 * Tests: history page access, search, filter, cancel booking
 */
public class BookingHistoryTests extends BaseTest {

    // Locators
    private final By searchInput = By.id("searchInput");
    private final By statusFilter = By.id("statusFilter");
    private final By slotFilter   = By.id("slotFilter");
    private final By bookingList  = By.id("bookingList");
    private final By totalBookings = By.id("totalBookings");
    private final By emptyState   = By.id("emptyState");
    private final By toastMessage = By.cssSelector(".toast .toast-message");

    @BeforeMethod
    public void setupLoggedInUser() {
        registerAndLoginUser();
    }

    /** Test history page loads when logged in */
    @Test(priority = 1, description = "History page accessible when logged in")
    public void testHistoryPageLoadsWhenLoggedIn() {
        navigateToHistory();
        waitFor(500);
        Assert.assertTrue(driver.getCurrentUrl().contains("history.html"),
            "History page should be accessible when logged in");
        WebElement container = driver.findElement(By.id("history-container"));
        Assert.assertTrue(container.isDisplayed(), "History container should be visible");
    }

    /** Test history page redirects to login when not logged in */
    @Test(priority = 2, description = "History page redirects when not logged in")
    public void testHistoryPageRedirectsWhenNotLoggedIn() {
        clearLocalStorage();
        navigateToHistory();
        waitFor(600);
        Assert.assertTrue(driver.getCurrentUrl().contains("login.html"),
            "Should redirect to login when not logged in");
    }

    /** Test empty state when no bookings exist */
    @Test(priority = 3, description = "Empty state shown when no bookings")
    public void testEmptyStateDisplayedWhenNoBookings() {
        setLocalStorageItem("allBookings", "[]");
        navigateToHistory();
        waitFor(500);
        WebElement empty = driver.findElement(emptyState);
        Assert.assertTrue(empty.isDisplayed(), "Empty state should be visible when no bookings exist");
    }

    /** Test summary cards are displayed */
    @Test(priority = 4, description = "Summary cards displayed on history page")
    public void testSummaryCardsDisplayed() {
        navigateToHistory();
        waitFor(500);
        WebElement totalEl = driver.findElement(totalBookings);
        Assert.assertTrue(totalEl.isDisplayed(), "Total bookings card should be visible");
    }

    /** Test history page shows a booking after one is made */
    @Test(priority = 5, description = "Booking appears in history after booking")
    public void testBookingAppearsInHistory() {
        // Inject a booking directly into localStorage
        String booking = "[{\"id\":\"BK001\",\"slot\":\"A1\",\"date\":\"2027-12-01\",\"time\":\"10:00\",\"status\":\"upcoming\",\"createdAt\":\"2027-01-01T10:00:00.000Z\"}]";
        setLocalStorageItem("allBookings", booking);

        navigateToHistory();
        waitFor(500);

        // Total should be 1
        WebElement totalEl = driver.findElement(totalBookings);
        Assert.assertEquals(totalEl.getText(), "1", "Total bookings count should be 1");

        // Booking card should be visible
        List<WebElement> cards = driver.findElements(By.cssSelector(".booking-card"));
        Assert.assertEquals(cards.size(), 1, "One booking card should be displayed");
    }

    /** Test search filters booking by slot name */
    @Test(priority = 6, description = "Search filters bookings by slot")
    public void testSearchFiltersBySlot() {
        // Inject two bookings
        String bookings = "[" +
            "{\"id\":\"BK001\",\"slot\":\"A1\",\"date\":\"2027-12-01\",\"time\":\"10:00\",\"status\":\"upcoming\",\"createdAt\":\"2027-01-01T10:00:00.000Z\"}," +
            "{\"id\":\"BK002\",\"slot\":\"C3\",\"date\":\"2027-12-02\",\"time\":\"11:00\",\"status\":\"upcoming\",\"createdAt\":\"2027-01-01T11:00:00.000Z\"}" +
        "]";
        setLocalStorageItem("allBookings", bookings);

        navigateToHistory();
        waitFor(500);

        // Search for "A1"
        driver.findElement(searchInput).sendKeys("A1");
        waitFor(400);

        // Only 1 card should remain visible
        List<WebElement> cards = driver.findElements(By.cssSelector(".booking-card"));
        Assert.assertEquals(cards.size(), 1, "Search should filter to 1 booking");
        Assert.assertTrue(cards.get(0).getText().contains("A1"), "Filtered card should be slot A1");
    }

    /** Test status filter shows only upcoming bookings */
    @Test(priority = 7, description = "Status filter shows only upcoming bookings")
    public void testStatusFilterShowsUpcoming() {
        String bookings = "[" +
            "{\"id\":\"BK001\",\"slot\":\"A1\",\"date\":\"2027-12-01\",\"time\":\"10:00\",\"status\":\"upcoming\",\"createdAt\":\"2027-01-01T10:00:00.000Z\"}," +
            "{\"id\":\"BK002\",\"slot\":\"B3\",\"date\":\"2020-01-01\",\"time\":\"09:00\",\"status\":\"past\",\"createdAt\":\"2020-01-01T09:00:00.000Z\"}" +
        "]";
        setLocalStorageItem("allBookings", bookings);

        navigateToHistory();
        waitFor(500);

        // Select "Upcoming" filter
        WebElement select = driver.findElement(statusFilter);
        select.findElement(By.xpath("//option[@value='upcoming']")).click();
        waitFor(400);

        List<WebElement> cards = driver.findElements(By.cssSelector(".booking-card.status-upcoming"));
        Assert.assertTrue(cards.size() >= 1, "Should show at least 1 upcoming booking");
    }

    /** Test slot row filter shows only Row A bookings */
    @Test(priority = 8, description = "Slot filter shows only Row A bookings")
    public void testSlotRowFilter() {
        String bookings = "[" +
            "{\"id\":\"BK001\",\"slot\":\"A2\",\"date\":\"2027-12-01\",\"time\":\"10:00\",\"status\":\"upcoming\",\"createdAt\":\"2027-01-01T10:00:00.000Z\"}," +
            "{\"id\":\"BK002\",\"slot\":\"C1\",\"date\":\"2027-12-02\",\"time\":\"11:00\",\"status\":\"upcoming\",\"createdAt\":\"2027-01-01T11:00:00.000Z\"}" +
        "]";
        setLocalStorageItem("allBookings", bookings);

        navigateToHistory();
        waitFor(500);

        WebElement select = driver.findElement(slotFilter);
        select.findElement(By.xpath("//option[@value='A']")).click();
        waitFor(400);

        List<WebElement> cards = driver.findElements(By.cssSelector(".booking-card"));
        Assert.assertEquals(cards.size(), 1, "Slot filter should show 1 Row-A booking");
    }

    /** Test cancel booking changes status */
    @Test(priority = 9, description = "Cancel booking changes its status to cancelled")
    public void testCancelBooking() {
        String booking = "[{\"id\":\"BK001\",\"slot\":\"A3\",\"date\":\"2027-12-01\",\"time\":\"10:00\",\"status\":\"upcoming\",\"createdAt\":\"2027-01-01T10:00:00.000Z\"}]";
        setLocalStorageItem("allBookings", booking);

        navigateToHistory();
        waitFor(500);

        // Click Cancel button
        WebElement cancelBtn = driver.findElement(By.cssSelector(".cancel-btn"));
        cancelBtn.click();
        waitFor(500);

        // Verify status changed in localStorage
        String stored = getLocalStorageItem("allBookings");
        Assert.assertTrue(stored.contains("cancelled"), "Booking status should be 'cancelled' in localStorage");
    }

    /** Test Pay button navigates to payment page */
    @Test(priority = 10, description = "Pay button navigates to payment page")
    public void testPayButtonNavigatesToPayment() {
        String booking = "[{\"id\":\"BK001\",\"slot\":\"A1\",\"date\":\"2027-12-01\",\"time\":\"10:00\",\"status\":\"upcoming\",\"createdAt\":\"2027-01-01T10:00:00.000Z\"}]";
        setLocalStorageItem("allBookings", booking);

        navigateToHistory();
        waitFor(500);

        WebElement payBtn = driver.findElement(By.cssSelector(".pay-btn"));
        payBtn.click();
        waitFor(800);

        Assert.assertTrue(driver.getCurrentUrl().contains("payment.html"),
            "Pay button should navigate to payment.html");
    }

    // ── Helpers ──────────────────────────────────────────────

    private void navigateToHistory() {
        navigateTo("history.html");
    }

    private void registerAndLoginUser() {
        // Use localStorage directly to avoid full registration flow
        clearLocalStorage();
        setLocalStorageItem("userEmail", VALID_EMAIL);
        setLocalStorageItem("userPassword", VALID_PASSWORD);
        setLocalStorageItem("userName", VALID_NAME);
        setLocalStorageItem("loggedIn", "true");
        navigateToHome();
    }
}
