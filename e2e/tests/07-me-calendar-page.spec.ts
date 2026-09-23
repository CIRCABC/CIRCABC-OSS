// Test #07: Personal Area - My Calendar page (view switching)
import { test, expect, env } from "./fixtures";

test.describe("Personal Area - Calendar", () => {
  test("should display the personal calendar and switch views", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["admin.username"], env["admin.password"]);

    await page.goto("/me/calendar");
    await page.waitForLoadState("networkidle");

    // Calendar container + title render
    await expect(page.locator(".calendar-container")).toBeVisible();
    await expect(page.locator(".page-header__title")).toBeVisible();

    // Default month view is present
    await expect(page.locator("#view")).toBeVisible();
    await checkA11yWithLogging();

    // Switch to day view
    await page.locator("#view").selectOption("day");
    await page.waitForLoadState("networkidle");
    await expect(page.locator("cbc-day")).toBeVisible();
    await checkA11yWithLogging();

    // Switch to week view
    await page.locator("#view").selectOption("week");
    await page.waitForLoadState("networkidle");
    await expect(page.locator("cbc-week")).toBeVisible();
    await checkA11yWithLogging();
  });
});
