package com.smartparking.tests;

import com.smartparking.base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * Test class for Payment and Fine Calculator functionality
 * Tests: page loads, fee calculation, fine calculation, payment methods,
 *        payment processing, modal display
 */
public class PaymentTests extends BaseTest {

    // Locators
    private final By durationSelect  = By.id("durationHours");
    private final By overstayInput   = By.id("overstayHours");
    private final By parkingFee      = By.id("parkingFee");
    private final By fineFee         = By.id("fineFee");
    private final By totalFee        = By.id("totalFee");
    private final By payBtn          = By.id("payBtn");
    private final By paymentModal    = By.id("paymentModal");
    private final By modalMessage    = By.id("modalMessage");
    private final By fineRow         = By.id("fineRow");
    private final By toastMsg        = By.cssSelector(".toast .toast-message");

    @BeforeMethod
    public void setupWithBooking() {
        clearLocalStorage();
        setLocalStorageItem("userEmail", VALID_EMAIL);
        setLocalStorageItem("userPassword", VALID_PASSWORD);
        setLocalStorageItem("userName", VALID_NAME);
        setLocalStorageItem("loggedIn", "true");

        // Set up a booking to pay for
        String booking = "[{\"id\":\"BK001\",\"slot\":\"A1\",\"date\":\"2027-12-01\",\"time\":\"10:00\",\"status\":\"upcoming\",\"createdAt\":\"2027-01-01T10:00:00.000Z\"}]";
        setLocalStorageItem("allBookings", booking);
        setLocalStorageItem("paymentBookingId", "BK001");
    }

    /** Test payment page loads when logged in */
    @Test(priority = 1, description = "Payment page loads when user is logged in")
    public void testPaymentPageLoads() {
        navigateTo("payment.html");
        waitFor(600);
        Assert.assertTrue(driver.getCurrentUrl().contains("payment.html"),
            "Payment page should be accessible");
        WebElement container = driver.findElement(By.id("payment-container"));
        Assert.assertTrue(container.isDisplayed(), "Payment container should be visible");
    }

    /** Test payment page redirects when not logged in */
    @Test(priority = 2, description = "Payment page redirects when not logged in")
    public void testPaymentRedirectsWhenNotLoggedIn() {
        clearLocalStorage();
        navigateTo("payment.html");
        waitFor(600);
        Assert.assertTrue(driver.getCurrentUrl().contains("login.html"),
            "Should redirect to login when not logged in");
    }

    /** Test default fee calculation (1 hour = ₹30) */
    @Test(priority = 3, description = "Default fee is ₹30 for 1 hour")
    public void testDefaultFeeCalculation() {
        navigateTo("payment.html");
        waitFor(600);

        WebElement feeEl = driver.findElement(parkingFee);
        Assert.assertEquals(feeEl.getText(), "₹30",
            "Default parking fee for 1 hour should be ₹30");

        WebElement totalEl = driver.findElement(totalFee);
        Assert.assertTrue(totalEl.getText().contains("30"),
            "Total fee should be ₹30 for 1 hour with no overstay");
    }

    /** Test fee increases with duration */
    @Test(priority = 4, description = "Fee scales correctly with duration selection")
    public void testFeeScalesWithDuration() {
        navigateTo("payment.html");
        waitFor(600);

        Select durationDropdown = new Select(driver.findElement(durationSelect));
        durationDropdown.selectByValue("3");
        waitFor(300);

        WebElement feeEl = driver.findElement(parkingFee);
        Assert.assertEquals(feeEl.getText(), "₹90",
            "Parking fee for 3 hours should be ₹90 (3 × ₹30)");
    }

    /** Test overstay fine is not shown when overstay is 0 */
    @Test(priority = 5, description = "Fine row is hidden when overstay is 0")
    public void testFineRowHiddenWhenNoOverstay() {
        navigateTo("payment.html");
        waitFor(600);

        WebElement fineRowEl = driver.findElement(fineRow);
        Assert.assertEquals(fineRowEl.getCssValue("display"), "none",
            "Fine row should be hidden when there is no overstay");
    }

    /** Test overstay fine appears and calculates correctly */
    @Test(priority = 6, description = "Overstay fine calculated correctly")
    public void testOverstayFineCalculation() {
        navigateTo("payment.html");
        waitFor(600);

        // Enter 2 hours overstay
        WebElement overstay = driver.findElement(overstayInput);
        overstay.clear();
        overstay.sendKeys("2");
        waitFor(300);

        // Fine should be 2 × ₹50 = ₹100
        WebElement fineFeeEl = driver.findElement(fineFee);
        Assert.assertEquals(fineFeeEl.getText(), "₹100",
            "Overstay fine for 2 hours should be ₹100 (2 × ₹50)");

        // Fine row should be visible
        WebElement fineRowEl = driver.findElement(fineRow);
        Assert.assertNotEquals(fineRowEl.getCssValue("display"), "none",
            "Fine row should be visible when overstay > 0");
    }

    /** Test total = parking fee + fine */
    @Test(priority = 7, description = "Total is sum of parking fee and fine")
    public void testTotalIncludesBothFeeAndFine() {
        navigateTo("payment.html");
        waitFor(600);

        // 2 hours duration = ₹60
        Select durationDropdown = new Select(driver.findElement(durationSelect));
        durationDropdown.selectByValue("2");
        waitFor(200);

        // 1 hour overstay = ₹50
        WebElement overstay = driver.findElement(overstayInput);
        overstay.clear();
        overstay.sendKeys("1");
        waitFor(300);

        // Total = ₹60 + ₹50 = ₹110
        WebElement totalEl = driver.findElement(totalFee);
        Assert.assertTrue(totalEl.getText().contains("110"),
            "Total should be ₹110 (₹60 parking + ₹50 fine)");
    }

    /** Test payment method selection */
    @Test(priority = 8, description = "Payment method can be selected")
    public void testPaymentMethodSelection() {
        navigateTo("payment.html");
        waitFor(600);

        // Click on Card payment
        WebElement cardOption = driver.findElement(By.id("pay-card"));
        cardOption.click();
        waitFor(200);

        Assert.assertTrue(cardOption.getAttribute("class").contains("selected"),
            "Card payment option should be marked as selected");

        // UPI should no longer be selected
        WebElement upiOption = driver.findElement(By.id("pay-upi"));
        Assert.assertFalse(upiOption.getAttribute("class").contains("selected"),
            "UPI option should be deselected after choosing Card");
    }

    /** Test all 4 payment methods are displayed */
    @Test(priority = 9, description = "All 4 payment methods are displayed")
    public void testAllPaymentMethodsDisplayed() {
        navigateTo("payment.html");
        waitFor(600);

        Assert.assertTrue(driver.findElement(By.id("pay-upi")).isDisplayed(), "UPI option should be visible");
        Assert.assertTrue(driver.findElement(By.id("pay-card")).isDisplayed(), "Card option should be visible");
        Assert.assertTrue(driver.findElement(By.id("pay-cash")).isDisplayed(), "Cash option should be visible");
        Assert.assertTrue(driver.findElement(By.id("pay-wallet")).isDisplayed(), "Wallet option should be visible");
    }

    /** Test payment processing shows success modal */
    @Test(priority = 10, description = "Payment processing shows success modal")
    public void testPaymentShowsSuccessModal() {
        navigateTo("payment.html");
        waitFor(600);

        // Click Pay button
        driver.findElement(payBtn).click();
        waitFor(2000); // wait for processing delay

        // Modal should appear
        WebElement modal = driver.findElement(paymentModal);
        Assert.assertTrue(modal.isDisplayed(), "Payment success modal should be visible after payment");
    }

    /** Test modal shows correct payment info */
    @Test(priority = 11, description = "Payment modal shows correct payment info")
    public void testPaymentModalShowsCorrectInfo() {
        navigateTo("payment.html");
        waitFor(600);

        driver.findElement(payBtn).click();
        waitFor(2000);

        WebElement msg = driver.findElement(modalMessage);
        Assert.assertTrue(msg.getText().contains("₹"),
            "Modal message should show the amount paid");
        Assert.assertTrue(msg.getText().toLowerCase().contains("upi") || msg.getText().toLowerCase().contains("paid"),
            "Modal should mention payment method or confirmation");
    }

    /** Test payment record saved in localStorage */
    @Test(priority = 12, description = "Payment record is saved in localStorage")
    public void testPaymentSavedToLocalStorage() {
        navigateTo("payment.html");
        waitFor(600);

        driver.findElement(payBtn).click();
        waitFor(2000);

        String payments = getLocalStorageItem("payments");
        Assert.assertNotNull(payments, "Payments should be saved in localStorage");
        Assert.assertTrue(payments.contains("TXN"), "Payment record should have a transaction ID");
        Assert.assertTrue(payments.contains("BK001"), "Payment should reference the correct booking ID");
    }
}
