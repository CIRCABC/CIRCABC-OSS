// Test #25: Library - Notification
import { test, expect, env } from "./fixtures";

test.describe("Library - Notification", () => {
  test("should configure notification settings", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Navigate to roles page
    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).click();
    await page.locator('[data-cy="library"]').click();

    // Go to notifications
    await page.locator('[data-cy="notifications"]').click();

    // Click the main CTA button
    await page.locator(".box > .cta").click();

    // Click search
    await page.locator("#search").click();

    // Select first option from dropdown
    const firstOption = await page.locator("#selectMultiple option").first();
    const value = await firstOption.getAttribute("value");
    await page.locator("#selectMultiple").selectOption(value);

    await page.waitForTimeout(500);

    // Add to list and confirm
    await page.locator("#addToList").click();
    await page.locator('[data-cy="ok"]').click();

    // Verify success and close
    await expect(page.getByText("Success")).toBeVisible();
    await page.getByText("Close").click();

    await checkA11yWithLogging();
  });
});
