// Test #15: Test FTP Access
import { test, expect, env } from "./fixtures";

test.describe("Test FTP Access", () => {
  test.fixme("should verify FTP access configuration", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("IGadmin1", "password123");

    // Navigate to "Test IG"
    await page.goto("/me/roles");
    await page.getByText("Test IG title").click();

    // Verify FTP access configuration
    await page.locator('[data-cy="admin"]').click();
    await page.locator('[data-cy="ftp-access"]').click();

    // Check FTP settings
    await expect(page.getByText("FTP Configuration")).toBeVisible();
    await expect(page.locator('[data-cy="ftp-enabled"]')).toBeVisible();

    await checkA11yWithLogging();
  });
});
