// System Message lifecycle — create → update → delete.
// Merged from former 80/81/82 specs and run with describe.serial so update
// and delete operate on the message created by the first step, and the
// chain retries together instead of racing across workers.
import { test, expect } from "./fixtures";

test.describe.serial("System Message - Lifecycle", () => {
  test("should create system message", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("admin", "admin");

    // Navigate to system messages
    await page.goto("/support/system-message");

    // Create message
    await page.locator(".cta").click();
    await page.waitForTimeout(500);
    await page.locator(".ql-editor").fill("My Message 1");
    await page.waitForTimeout(500);
    await page.locator(".cta").click();

    // Verify success
    await expect(
      page.getByLabel("Notifications").getByText("Success"),
    ).toBeVisible();

    await checkA11yWithLogging();
  });

  test("should update system message", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("admin", "admin");

    // Navigate to system messages
    await page.goto("/support/system-message");

    // Update message
    await page
      .locator(
        ':nth-child(1) > cbc-template-renderer > .template > .actions > [data-cy="update-message"]',
      )
      .click();
    await page.waitForTimeout(500);
    await page.locator(".ql-editor").fill(" Updated");
    await page.waitForTimeout(500);
    await page.locator(".cta").click();

    // Verify success
    await expect(
      page.getByLabel("Notifications").getByText("Success"),
    ).toBeVisible();

    await checkA11yWithLogging();
  });

  test("should delete system message", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("admin", "admin");

    // Navigate to system messages
    await page.goto("/support/system-message");

    // Delete message
    await page.locator("[data-cy=delete-inline]").click();
    await page.locator("#confirmDeletePerm").click();

    // Verify success
    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });
});
