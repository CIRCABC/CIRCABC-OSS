// Test #91a: Help - Contact support page (public route, no dependencies)
import { test, expect } from "./fixtures";

test.describe("Help - Contact Support", () => {
  test("should display the contact support form", async ({
    page,
    checkA11yWithLogging,
  }) => {
    await page.goto("/help/contact");
    await page.waitForLoadState("networkidle");

    // Contact form renders with the reason select.
    await expect(page.locator(".box .title")).toBeVisible();
    await expect(page.locator("#reason")).toBeVisible();

    await checkA11yWithLogging();
  });
});
