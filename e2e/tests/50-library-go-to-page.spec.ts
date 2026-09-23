// Test #24: Library - Go To
import { test, expect, env } from "./fixtures";

test.describe("Library - Go To", () => {
  test("should navigate to library page", async ({
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

    // Click on library section
    await page.locator('[data-cy="library"]').click();

    await checkA11yWithLogging();
  });
});
