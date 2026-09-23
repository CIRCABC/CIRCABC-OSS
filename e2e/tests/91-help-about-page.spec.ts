// Test #91: Help - About page (public route, no dependencies)
import { test, expect } from "./fixtures";

test.describe("Help - About", () => {
  test("should display the about page", async ({
    page,
    checkA11yWithLogging,
  }) => {
    await page.goto("/help/about");
    await page.waitForLoadState("networkidle");

    // About content renders (logo + heading).
    await expect(page.getByRole("heading", { name: /about/i })).toBeVisible();

    await checkA11yWithLogging();
  });
});
