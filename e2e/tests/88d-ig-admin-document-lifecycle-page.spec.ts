// Test #88d: Interest Group Admin - Document lifecycle page
import { test, expect, env } from "./fixtures";

test.describe("IG Admin - Document Lifecycle", () => {
  test("should display the document lifecycle page", async ({
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

    await page.locator('[data-cy="admin-tab-documents"]').click();
    await page.waitForLoadState("networkidle");

    await expect(page).toHaveURL(/\/admin\/documents/);
    await expect(page.locator(".page-header__title")).toBeVisible();

    await checkA11yWithLogging();
  });
});
