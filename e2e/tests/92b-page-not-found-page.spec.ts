// Test #92b: Page not found (404) - wildcard catch-all route
import { test, expect } from "./fixtures";

test.describe("Error - Page Not Found", () => {
  test("should display the 404 page for an unknown route", async ({
    page,
    checkA11yWithLogging,
  }) => {
    await page.goto("/this-route-does-not-exist-e2e");
    await page.waitForLoadState("networkidle");

    await expect(page.locator(".not-found-container")).toBeVisible();
    await expect(page.getByRole("heading").first()).toBeVisible();

    await checkA11yWithLogging();
  });
});
