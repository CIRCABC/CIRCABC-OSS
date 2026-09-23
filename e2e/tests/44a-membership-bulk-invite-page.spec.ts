// Test #44a: Membership - Bulk invite page
//
// Depends on: IG + membership (tests 41-43). Login as the IG admin.
// The form is asserted only (not submitted) - a full bulk-invite requires a
// prepared spreadsheet/CSV import and notification infra.
import { test, expect, env } from "./fixtures";

test.describe("Membership - Bulk Invite", () => {
  test("should display the bulk invite page", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Navigate to the IG members, then the bulk-invite sub-page.
    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).click();
    await page.locator('[data-cy="members"]').click();
    await page.waitForLoadState("networkidle");

    await page
      .getByRole("link", { name: /bulk invite/i })
      .first()
      .click();
    await page.waitForLoadState("networkidle");

    // On the bulk-invite route with the import form
    await expect(page).toHaveURL(/\/members\/bulk-invite\//);
    await expect(page.locator("#file")).toBeAttached();
    await expect(page.locator("table.main")).toBeVisible();

    await checkA11yWithLogging();
  });
});
