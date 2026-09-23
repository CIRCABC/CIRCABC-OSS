// Test #88f: Interest Group Admin - Logos / appearance page
import { test, expect, env } from "./fixtures";

test.describe("IG Admin - Logos", () => {
  test("should display the logos page", async ({
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

    await page.locator('[data-cy="admin-tab-logos"]').click();
    await page.waitForLoadState("networkidle");

    await expect(page).toHaveURL(/\/admin\/logos/);
    await expect(page.locator(".page-header__title")).toBeVisible();

    await checkA11yWithLogging();
  });
});
