// Test #88i: Interest Group Admin - Auto-upload configuration page
//
// Marked fixme: exercising auto-upload requires an external FTP endpoint that
// is not available in the local/test environment.
import { test, expect, env } from "./fixtures";

test.describe("IG Admin - Auto Upload", () => {
  test.fixme("should display the auto-upload configuration page", async ({
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

    await page.locator('[data-cy="autoupload-tab"]').click();
    await page.waitForLoadState("networkidle");

    await expect(page).toHaveURL(/\/admin\/auto-upload/);
    await expect(page.locator('[data-cy="add-configuration"]')).toBeVisible();

    await checkA11yWithLogging();
  });
});
