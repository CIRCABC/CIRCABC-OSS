// Test #8: Create Header
import { test, expect, env } from "./fixtures";

test.describe("Create Header", () => {
  test("should create header", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["admin.username"], env["admin.password"]);

    // Navigate to admin / headers
    await page.goto("/admin/headers");

    // Check if header already exists (idempotent)
    const headerExists = await page
      .getByText(env["header.name"])
      .first()
      .isVisible()
      .catch(() => false);
    if (headerExists) {
      await checkA11yWithLogging();
      return; // Skip - header already exists
    }

    // Click create header
    await page.locator('[data-cy="add-header"]').click();

    // Fill form
    await page.locator('[data-cy="name"]').fill(env["header.name"]);
    await page
      .locator('[data-cy="description"]')
      .locator('[data-cy="text"]')
      .fill(env["header.description"]);
    await page.locator('[data-cy="ok"]').click();

    // Verify success message or header appears in list
    await expect(page.getByText(env["header.name"]).first()).toBeVisible();

    // Close any notifications
    await page
      .locator("text=Close")
      .click()
      .catch(() => {});

    await checkA11yWithLogging();
  });
});
