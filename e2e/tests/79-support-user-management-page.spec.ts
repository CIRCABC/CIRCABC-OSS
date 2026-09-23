// Test #79: Support - User management page
//
// Uses CircabcAdmin (CIRCABC administrator, created test 10 & granted test 20)
// which satisfies the app-admin guard on the support area.
import { test, expect, env } from "./fixtures";

test.describe("Support - User Management", () => {
  test("should display the user management page", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["circabc.admin.username"], env["circabc.admin.password"]);

    await page.goto("/support/user-management");
    await page.waitForLoadState("networkidle");

    // Search container renders
    await expect(page.locator(".user-mgmt--container")).toBeVisible();
    await expect(page.locator(".user-mgmt-search")).toBeVisible();

    await checkA11yWithLogging();
  });
});
