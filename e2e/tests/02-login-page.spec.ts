// spec: docs/migration-prd.md
// Test #2: Login Page - admin user login flow

import { test, expect, env } from "./fixtures";

test.describe("Login Page", () => {
  test("should login successfully with admin credentials", async ({
    page,
    checkA11yWithLogging,
  }) => {
    // Navigate to home page
    await page.goto("/welcome");

    // Click Connect button
    await page.getByRole("link", { name: "Connect" }).click();

    // Fill login form
    await page.locator('[data-cy="username"]').fill("admin");
    await page.locator('[data-cy="password"]').fill("admin");
    await page.locator('[data-cy="login"]').click();

    // Verify successful login - redirect to dashboard
    await expect(page).toHaveURL(/\/me/);
    await expect(
      page.getByRole("heading", { name: "Dashboard" }),
    ).toBeVisible();

    // Run accessibility check
    await checkA11yWithLogging();
  });
});
