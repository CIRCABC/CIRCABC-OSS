// Test #88a: Interest Group Admin - General settings page
//
// Runs after the dynamic-authority tests (88) and before IG deletion (89).
// Login as the IG admin.
import { test, expect, env } from "./fixtures";

test.describe("IG Admin - General", () => {
  test("should display the general settings page", async ({
    page,
    login,
    checkA11yWithLogging,
    navigateToIGSection,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    await navigateToIGSection("admin");

    await page.locator('[data-cy="admin-tab-general"]').click();
    await page.waitForLoadState("networkidle");

    await expect(page).toHaveURL(/\/admin\/general/);
    await expect(page.locator(".page-header__title")).toBeVisible();

    await checkA11yWithLogging();
  });
});
