// Test #79b: Support - Distribution list page
//
// Uses CircabcAdmin (CIRCABC administrator) which satisfies the app-admin guard.
import { test, expect, env } from "./fixtures";

test.describe("Support - Distribution List", () => {
  test("should display the distribution list page", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["circabc.admin.username"], env["circabc.admin.password"]);

    await page.goto("/support/distribution-list");
    await page.waitForLoadState("networkidle");

    // Distribution list actions + listing render
    await expect(page.locator(".right-actions")).toBeVisible();
    await expect(page.locator(".box--alternate-lines")).toBeVisible();

    await checkA11yWithLogging();
  });
});
