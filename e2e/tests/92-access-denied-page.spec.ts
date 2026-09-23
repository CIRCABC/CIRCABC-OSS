// Test #92: Access denied page (public route, no dependencies)
import { test, expect } from "./fixtures";

test.describe("Error - Access Denied", () => {
  test("should display the access denied page", async ({
    page,
    checkA11yWithLogging,
  }) => {
    await page.goto("/denied");
    await page.waitForLoadState("networkidle");

    await expect(page.locator(".access-denied-container")).toBeVisible();
    await expect(page.getByRole("heading").first()).toBeVisible();

    await checkA11yWithLogging();
  });
});
