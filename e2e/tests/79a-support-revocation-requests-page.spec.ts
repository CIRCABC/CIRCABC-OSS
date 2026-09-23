// Test #79a: Support - Revocation requests page
//
// Uses CircabcAdmin (CIRCABC administrator) which satisfies the app-admin guard.
import { test, expect, env } from "./fixtures";

test.describe("Support - Revocation Requests", () => {
  test("should display the revocation requests page", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["circabc.admin.username"], env["circabc.admin.password"]);

    await page.goto("/support/revocation-requests");
    await page.waitForLoadState("networkidle");

    // Revocation jobs table container renders
    await expect(page.locator(".table-container")).toBeVisible();

    await checkA11yWithLogging();
  });
});
