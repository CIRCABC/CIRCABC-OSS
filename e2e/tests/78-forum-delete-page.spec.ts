// Test #78: Forum - Delete (moved from 75 - must run AFTER topic tests 76-77)
import { test, expect, env } from "./fixtures";

test.describe("Forum - Delete", () => {
  test("should delete forum", async ({ page, login, checkA11yWithLogging }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Navigate to IG → Forums
    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).click();
    await page.locator("[data-cy=forums]").click();

    // Delete forum
    await page.getByText("Forum1").click();
    await page.getByText("Delete").click();
    await page.locator(".cta").getByText("Delete").click();

    // Verify success
    await expect(page.getByText("Success")).toBeVisible();
    await page.getByText("Close").click();

    await checkA11yWithLogging();
  });
});
