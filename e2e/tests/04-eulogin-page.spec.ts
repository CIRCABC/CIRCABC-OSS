// Test #3: EU Login Page
import { test, expect, env } from "./fixtures";

test.describe("EU Login Page", () => {
  test.fixme("should login via EU Login SSO", async ({
    page,
    checkA11yWithLogging,
  }) => {
    // EU Login SSO requires external service configuration
    // Navigate to home page
    await page.goto("/");

    // Click EU Login SSO button
    await page.locator('[data-cy="eu-login"]').click();

    // Handle cross-origin redirect to EU Login
    await page.waitForURL("**/ecas/**");

    // Enter credentials on EU Login page
    await page.locator("#username").fill(env["admin.username"]);
    await page.locator("#password").fill(env["admin.password"]);
    await page.locator('[type="submit"]').click();

    // Verify redirect back to CircaBC dashboard
    await page.waitForURL("**/me");
    await expect(page.getByText("Dashboard")).toBeVisible();

    await checkA11yWithLogging();
  });
});
