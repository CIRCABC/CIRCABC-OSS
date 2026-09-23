// Test #4: Logout Page
import { test, expect, env } from "./fixtures";

test.describe("Logout Page", () => {
  test("should logout successfully", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["admin.username"], env["admin.password"]);
    await page.goto("/me");

    // Navigate to logout URL (matches Cypress approach)
    await page.goto("/login/logout", { waitUntil: "networkidle" });

    // Verify redirected to home page
    await expect(page).toHaveURL(/\/welcome/, { timeout: 15000 });

    // Verify user is no longer authenticated
    await page.goto("/me", { waitUntil: "networkidle" });
    await expect(page).toHaveURL(/\/welcome/, { timeout: 15000 });

    await checkA11yWithLogging();
  });
});
