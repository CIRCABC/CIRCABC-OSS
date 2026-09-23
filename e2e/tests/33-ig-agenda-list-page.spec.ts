// Test #33: Interest Group - Agenda list view
//
// Depends on: IG created (test 30). Login as the IG admin.
import { test, expect, env } from "./fixtures";

test.describe("Interest Group - Agenda List", () => {
  test("should display the agenda list view", async ({
    page,
    login,
    checkA11yWithLogging,
    navigateToIGSection,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    await navigateToIGSection("agenda");

    // Switch to the list view via the "view as list" link.
    await page.getByRole("link", { name: /list/i }).first().click();
    await page.waitForLoadState("networkidle");

    // On the agenda list route with a table
    await expect(page).toHaveURL(/\/agenda\/list/);
    await expect(page.locator("table.main")).toBeVisible();

    await checkA11yWithLogging();
  });
});
