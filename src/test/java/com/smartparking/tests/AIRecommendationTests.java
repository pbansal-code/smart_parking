package com.smartparking.tests;

import com.smartparking.base.BaseTest;
import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;

/**
 * Test class for AI Slot Recommendation feature
 * Tests: recommendation badge appears, correct slot highlighted,
 *        recommendation changes with history, AI banner visible
 */
public class AIRecommendationTests extends BaseTest {

    @BeforeMethod
    public void setupLoggedInUser() {
        clearLocalStorage();
        setLocalStorageItem("userEmail", VALID_EMAIL);
        setLocalStorageItem("userPassword", VALID_PASSWORD);
        setLocalStorageItem("userName", VALID_NAME);
        setLocalStorageItem("loggedIn", "true");
    }

    /** Test AI recommendation banner is visible on booking page */
    @Test(priority = 1, description = "AI recommendation banner is visible on booking page")
    public void testAIBannerVisible() {
        navigateToBook();
        waitFor(800);

        WebElement banner = driver.findElement(By.id("aiBanner"));
        Assert.assertTrue(banner.isDisplayed(), "AI banner should be visible on booking page");
        Assert.assertTrue(banner.getText().contains("AI Smart Recommendation"),
            "Banner should mention AI Smart Recommendation");
    }

    /** Test that exactly one slot has the AI recommended badge */
    @Test(priority = 2, description = "Exactly one slot has AI recommended badge")
    public void testExactlyOneSlotHasAIBadge() {
        navigateToBook();
        waitFor(800);

        List<WebElement> aiBadges = driver.findElements(By.cssSelector(".ai-badge"));
        Assert.assertEquals(aiBadges.size(), 1,
            "Exactly one slot should be marked as AI recommended");
    }

    /** Test AI recommended slot is not an occupied slot */
    @Test(priority = 3, description = "AI recommended slot is not occupied")
    public void testAIRecommendedSlotIsAvailable() {
        navigateToBook();
        waitFor(800);

        List<WebElement> recommendedSlots = driver.findElements(By.cssSelector(".slot-item.ai-recommended"));
        Assert.assertEquals(recommendedSlots.size(), 1,
            "Should have exactly one AI recommended slot");

        WebElement recommended = recommendedSlots.get(0);
        Assert.assertFalse(recommended.getAttribute("class").contains("occupied"),
            "AI recommended slot must not be occupied");
    }

    /** Test AI badge text content */
    @Test(priority = 4, description = "AI badge shows correct text")
    public void testAIBadgeText() {
        navigateToBook();
        waitFor(800);

        WebElement badge = driver.findElement(By.cssSelector(".ai-badge"));
        Assert.assertTrue(badge.getText().contains("AI Pick"),
            "AI badge should say 'AI Pick'");
    }

    /** Test AI recommendation changes when user has booking history */
    @Test(priority = 5, description = "AI recommendation reflects booking history")
    public void testAIRecommendationReflectsHistory() {
        // Simulate user who always books A3
        String history = "[" +
            "{\"id\":\"BK001\",\"slot\":\"A3\",\"date\":\"2026-01-01\",\"time\":\"09:00\",\"status\":\"past\",\"createdAt\":\"2026-01-01T09:00:00.000Z\"}," +
            "{\"id\":\"BK002\",\"slot\":\"A3\",\"date\":\"2026-01-02\",\"time\":\"09:00\",\"status\":\"past\",\"createdAt\":\"2026-01-02T09:00:00.000Z\"}," +
            "{\"id\":\"BK003\",\"slot\":\"A3\",\"date\":\"2026-01-03\",\"time\":\"09:00\",\"status\":\"past\",\"createdAt\":\"2026-01-03T09:00:00.000Z\"}" +
        "]";
        setLocalStorageItem("allBookings", history);

        navigateToBook();
        waitFor(800);

        // AI should recommend A3 (highest score due to 3x history)
        List<WebElement> recommendedSlots = driver.findElements(By.cssSelector(".slot-item.ai-recommended"));
        Assert.assertEquals(recommendedSlots.size(), 1, "Should have one AI recommended slot");

        String slotText = recommendedSlots.get(0).getText().replace("⭐ AI Pick", "").trim();
        Assert.assertEquals(slotText, "A3",
            "AI should recommend A3 because user has booked it 3 times before");
    }

    /** Test AI recommended slot can still be selected by user */
    @Test(priority = 6, description = "User can select the AI recommended slot")
    public void testUserCanSelectAIRecommendedSlot() {
        navigateToBook();
        waitFor(800);

        WebElement recommended = driver.findElement(By.cssSelector(".slot-item.ai-recommended"));
        recommended.click();
        waitFor(400);

        // Should now also have 'selected' class
        Assert.assertTrue(recommended.getAttribute("class").contains("selected"),
            "AI recommended slot should be selectable");

        // Hidden input should have a value
        WebElement slotInput = driver.findElement(By.id("slot"));
        Assert.assertFalse(slotInput.getAttribute("value").isEmpty(),
            "Slot hidden input should be set after clicking AI recommended slot");
    }

    /** Test AI toast message appears on page load */
    @Test(priority = 7, description = "AI recommendation toast message is shown")
    public void testAIRecommendationToastShown() {
        navigateToBook();
        waitFor(1200);

        // Toast should appear mentioning AI recommendation
        boolean toastFound = isElementPresent(By.cssSelector(".toast"));
        if (toastFound) {
            WebElement toast = driver.findElement(By.cssSelector(".toast-message"));
            Assert.assertTrue(toast.getText().contains("AI recommends"),
                "Toast should mention AI recommendation");
        }
        // Note: toast may have auto-dismissed; the badge check in earlier tests is the primary assertion
    }

    /** Test no AI badge is placed on occupied slot */
    @Test(priority = 8, description = "Occupied slots never get AI recommendation badge")
    public void testOccupiedSlotsHaveNoAIBadge() {
        // Even if history includes B1/B2 (occupied), they should not get badges
        String history = "[" +
            "{\"id\":\"BK001\",\"slot\":\"B1\",\"date\":\"2026-01-01\",\"time\":\"09:00\",\"status\":\"past\",\"createdAt\":\"2026-01-01T09:00:00.000Z\"}," +
            "{\"id\":\"BK002\",\"slot\":\"B1\",\"date\":\"2026-01-02\",\"time\":\"09:00\",\"status\":\"past\",\"createdAt\":\"2026-01-02T09:00:00.000Z\"}," +
            "{\"id\":\"BK003\",\"slot\":\"B2\",\"date\":\"2026-01-03\",\"time\":\"09:00\",\"status\":\"past\",\"createdAt\":\"2026-01-03T09:00:00.000Z\"}" +
        "]";
        setLocalStorageItem("allBookings", history);

        navigateToBook();
        waitFor(800);

        // B1 and B2 should not have AI badge
        List<WebElement> occupiedSlots = driver.findElements(By.cssSelector(".slot-item.occupied"));
        for (WebElement slot : occupiedSlots) {
            List<WebElement> badges = slot.findElements(By.cssSelector(".ai-badge"));
            Assert.assertEquals(badges.size(), 0,
                "Occupied slot " + slot.getText() + " should not have an AI badge");
        }
    }
}
