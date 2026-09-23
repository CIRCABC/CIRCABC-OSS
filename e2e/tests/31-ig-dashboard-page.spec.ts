// Test #31: Interest Group - Dashboard page
//
// Depends on: IG created (test 30). Login as the IG admin.
import { test, expect, env } from "./fixtures";

test.describe("Interest Group - Dashboard", () => {
  test("should display the interest group dashboard", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Navigate to the IG (lands on the group dashboard).
    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).click();
    await page.waitForLoadState("networkidle");

    // On a group route
    await expect(page).toHaveURL(/\/group\//);

    // Dashboard containers render (description + contact/location).
    await expect(page.locator(".dashboard-container")).toBeVisible();
    await expect(page.locator("cbc-group-desciptor")).toBeVisible();

    await checkA11yWithLogging();
  });
});
