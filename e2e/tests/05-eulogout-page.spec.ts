// Test #5: EU Logout Page
import { test, expect, env } from "./fixtures";

test.describe("EU Logout Page", () => {
  test.fixme("should logout via EU Login SSO", async ({
    page,
    checkA11yWithLogging,
  }) => {
    // EU Login/Logout SSO requires external service configuration
    // Login via EU Login first
    await page.goto("/");
    await page.locator('[data-cy="eu-login"]').click();
    await page.waitForURL("**/ecas/**");
    await page.locator("#username").fill(env["admin.username"]);
    await page.locator("#password").fill(env["admin.password"]);
    await page.locator('[type="submit"]').click();
    await page.waitForURL("**/me");

    // Trigger EU logout
    await page.locator('[data-cy="user-menu"]').click();
    await page.locator('[data-cy="eu-logout"]').click();

    // Verify SSO session terminated and redirected to home
    await page.waitForURL("**/welcome");
    await expect(page.getByText("CIRCABC")).toBeVisible();

    await checkA11yWithLogging();
  });
});
