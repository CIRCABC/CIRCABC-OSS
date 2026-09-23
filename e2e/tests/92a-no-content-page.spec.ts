// Test #92a: No content found page (public route, no dependencies)
import { test, expect } from "./fixtures";

test.describe("Error - No Content", () => {
  test("should display the no-content page", async ({
    page,
    checkA11yWithLogging,
  }) => {
    await page.goto("/no-content");
    await page.waitForLoadState("networkidle");

    await expect(page.locator(".not-found-container")).toBeVisible();
    await expect(page.getByRole("heading").first()).toBeVisible();

    await checkA11yWithLogging();
  });
});
