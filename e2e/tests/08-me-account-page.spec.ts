// Test #08: Personal Area - Account settings page
import { test, expect, env } from "./fixtures";

test.describe("Personal Area - Account", () => {
  test("should display the account settings form", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["admin.username"], env["admin.password"]);

    await page.goto("/me/account");
    await page.waitForLoadState("networkidle");

    // The account box renders
    await expect(page.locator(".account-box")).toBeVisible();

    // Dismiss the "updating user" alert to reveal the form (idempotent - alert
    // may already be dismissed).
    const agreeButton = page
      .locator(".alertButtonBox > .button")
      .filter({ hasText: "OK" });
    if (await agreeButton.isVisible().catch(() => false)) {
      await agreeButton.click();
    }

    // The account form is now visible
    await expect(
      page.locator("form.account-form, .account-form"),
    ).toBeVisible();

    await checkA11yWithLogging();
  });
});
