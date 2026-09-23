// Test #29: Library - Create Space
import { test, expect, env } from "./fixtures";

test.describe("Library - Create Space", () => {
  test("should create space/folder", async ({
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

    // Create folder
    await page.locator("[data-cy=add]").click();
    await page.locator("[data-cy=folder]").click({ force: true });
    await page.locator("[data-cy=name]").fill("Space 1");
    await page.locator("[data-cy=text]").fill("Space 1 Title");
    await page.locator(".ql-editor").fill("Space 1 Description");
    await page.locator("[data-cy=ok]").click();

    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });
});
