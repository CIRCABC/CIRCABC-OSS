// Test #32: Interest Group - Agenda calendar view
//
// Depends on: IG created (test 30). Login as the IG admin.
import { test, expect, env } from "./fixtures";

test.describe("Interest Group - Agenda Calendar", () => {
  test("should display the agenda calendar", async ({
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

    // Agenda calendar container renders
    await expect(page.locator(".calendar-container")).toBeVisible();
    await expect(page.locator(".page-header__title")).toBeVisible();

    await checkA11yWithLogging();
  });
});
