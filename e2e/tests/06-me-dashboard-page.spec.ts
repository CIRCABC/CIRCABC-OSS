// Test #06: Personal Area - User Dashboard page
import { test, expect, env } from "./fixtures";

test.describe("Personal Area - Dashboard", () => {
  test("should display the user dashboard with its sections", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["admin.username"], env["admin.password"]);

    await page.goto("/me");
    await page.waitForLoadState("networkidle");

    // Dashboard heading renders
    await expect(
      page.getByRole("heading", { name: "Dashboard" }),
    ).toBeVisible();

    // Dashboard dashlets/containers render (favourites, timelines, memberships)
    await expect(page.locator("cbc-user-favourites")).toBeVisible();
    await expect(page.locator("cbc-user-memberships")).toBeVisible();
    await expect(page.locator("cbc-user-timeline").first()).toBeVisible();

    await checkA11yWithLogging();
  });
});
