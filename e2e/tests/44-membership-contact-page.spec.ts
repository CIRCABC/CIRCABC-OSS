// Test #44: Membership - Contact page (send email to members)
//
// Depends on: IG + membership (tests 41-43). Login as the IG admin.
import { test, expect, env } from "./fixtures";

test.describe("Membership - Contact", () => {
  test("should display the members contact page", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Navigate to the IG members, then the contact (send email) sub-page.
    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).click();
    await page.locator('[data-cy="members"]').click();
    await page.waitForLoadState("networkidle");

    await page
      .getByRole("link", { name: /contact users/i })
      .first()
      .click();
    await page.waitForLoadState("networkidle");

    // On the contact route with the email form tabs
    await expect(page).toHaveURL(/\/members\/contact/);
    await expect(page.locator(".box--tabs")).toBeVisible();
    await expect(page.locator("#subject")).toBeVisible();

    await checkA11yWithLogging();
  });
});
