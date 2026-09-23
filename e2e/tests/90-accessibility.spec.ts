// Test #90: Accessibility
import { test, expect } from "./fixtures";

test.describe("Accessibility", () => {
  test.beforeEach(async ({ login }) => {
    await login("IGadmin1", "password123");
  });

  test("home page", async ({ page, checkA11yWithLogging }) => {
    await page.goto("/");
    await checkA11yWithLogging();
  });

  test("user dashboard", async ({ page, checkA11yWithLogging }) => {
    await page.goto("/me");
    await checkA11yWithLogging();
  });

  test("user calendar", async ({ page, checkA11yWithLogging }) => {
    await page.goto("/me/calendar");
    await checkA11yWithLogging();
  });

  test("user calendar day", async ({ page, checkA11yWithLogging }) => {
    await page.goto("/me/calendar");
    await page.locator("#view").selectOption("day");
    await checkA11yWithLogging();
  });

  test("user calendar week", async ({ page, checkA11yWithLogging }) => {
    await page.goto("/me/calendar");
    await page.locator("#view").selectOption("week");
    await checkA11yWithLogging();
  });

  test("user roles", async ({ page, checkA11yWithLogging }) => {
    await page.goto("/me/roles");
    await checkA11yWithLogging();
  });

  test("user explore", async ({ page, checkA11yWithLogging }) => {
    await page.goto("/explore");
    await checkA11yWithLogging();
    await page.locator(".link").click();
    await checkA11yWithLogging();
    await page.locator(".link").click();
    await checkA11yWithLogging();
  });
});
