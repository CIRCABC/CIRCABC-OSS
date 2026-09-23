// Test #88j: Interest Group Admin - External repository page
//
// Marked fixme: the external-repository tab is only rendered when the external
// repository feature is enabled, which requires an external service that is not
// available in the local/test environment.
import { test, expect, env } from "./fixtures";

test.describe("IG Admin - External Repository", () => {
  test.fixme("should display the external repository page", async ({
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

    await page.locator('[data-cy="admin-tab-external-repository"]').click();
    await page.waitForLoadState("networkidle");

    await expect(page).toHaveURL(/\/admin\/external-repository/);
    await expect(page.locator(".page-header__title")).toBeVisible();

    await checkA11yWithLogging();
  });
});
