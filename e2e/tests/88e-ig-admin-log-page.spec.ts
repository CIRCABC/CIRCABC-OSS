// Test #88e: Interest Group Admin - Activity log page
import { test, expect, env } from "./fixtures";

test.describe("IG Admin - Log", () => {
  test("should display the activity log page", async ({
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

    await page.locator('[data-cy="admin-tab-log"]').click();
    await page.waitForLoadState("networkidle");

    await expect(page).toHaveURL(/\/admin\/log/);
    await expect(page.locator(".page-header__title")).toBeVisible();

    await checkA11yWithLogging();
  });
});
