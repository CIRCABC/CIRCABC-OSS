// Test #91b: Help - Legal notice page (public route, no dependencies)
import { test, expect } from "./fixtures";

test.describe("Help - Legal Notice", () => {
  test("should display the legal notice page", async ({
    page,
    checkA11yWithLogging,
  }) => {
    await page.goto("/help/legal-notice");
    await page.waitForLoadState("networkidle");

    // Legal notice step navigation renders.
    const main = page.locator("#main-content");
    await expect(
      main.getByText("Privacy statement", { exact: true }),
    ).toBeVisible();
    await expect(
      main.getByText("Accessibility statement", { exact: true }),
    ).toBeVisible();

    await checkA11yWithLogging();
  });
});
