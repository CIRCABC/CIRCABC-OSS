// Test #30: Library - Set Favorite Space
import { test, expect, env } from "./fixtures";

test.describe("Library - Set Favorite Space", () => {
  test("should mark space as favorite", async ({
    page,
    login,
    checkA11yWithLogging,
    navigateToIGSection,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    await navigateToIGSection("library");

    // Click favorite star on second row
    await page
      .locator(
        ":nth-child(2) > .cell-file-name > .file-name > cbc-favourite-switch > .favourite-container > a > img",
      )
      .click();

    // Verify on dashboard
    await page.goto("/me");
    await expect(page.getByText("Space 1")).toBeVisible();

    await checkA11yWithLogging();
  });
});
