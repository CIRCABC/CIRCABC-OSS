// Test #32: Library - Delete All
import { test, expect, env } from "./fixtures";

test.describe("Library - Delete All", () => {
  test("should delete all items", async ({
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

    // Select all and delete
    await page.locator("[data-cy=select-all]").click();
    await page.locator("[data-cy=delete-all]").click();
    await page.locator("[data-cy=delete]").click();

    await expect(page.getByText("Success")).toBeVisible();
    await page.getByRole("main").getByText("Close").click({ force: true });

    await checkA11yWithLogging();
  });
});
