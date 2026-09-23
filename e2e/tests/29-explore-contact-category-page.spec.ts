// Test #29: Explore - Contact category administrators page
//
// Depends on: header/category (tests 21-22) and the Access user (test 10).
// The form is asserted only (not submitted) since sending requires a mail
// service that may not be available locally.
import { test, expect, env } from "./fixtures";

test.describe("Explore - Contact Category", () => {
  test("should display the contact-category form", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["access.username"], env["access.password"]);

    await page.goto("/explore/contact-category");
    await page.waitForLoadState("networkidle");

    // Form and its selects render
    await expect(page.locator(".box .title")).toBeVisible();
    await expect(page.locator("#header")).toBeVisible();
    await expect(page.locator("#category")).toBeVisible();
    await expect(page.locator("#messageContent")).toBeVisible();

    await checkA11yWithLogging();
  });
});
