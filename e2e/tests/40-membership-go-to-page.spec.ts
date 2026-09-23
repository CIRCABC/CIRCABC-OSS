// Test #20: Membership - Go To
import { test, expect, env } from "./fixtures";

test.describe("Membership - Go To", () => {
  test("should navigate to membership page", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Navigate to roles page
    await page.goto("/me/roles");

    // Click on the interest group
    await page.getByText(env["interest.group.title"]).click();

    // Click on members section
    await page.locator('[data-cy="members"]').click();

    await checkA11yWithLogging();
  });
});
