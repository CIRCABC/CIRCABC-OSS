// Test #28: Library - Copy Paste
import { test, expect, env } from "./fixtures";

test.describe("Library - Copy Paste", () => {
  test("should copy and paste file", async ({
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

    // Select file and add to clipboard
    await page.locator("[data-cy=row-checkbox]").first().click();
    await expect(page.locator("[data-cy=add-to-clipboard]")).toBeVisible({ timeout: 10000 });
    await page.locator("[data-cy=add-to-clipboard]").click();

    // Open clipboard and copy/link
    await page.locator("[data-cy=open-clipboard]").click();
    await page.locator("[data-cy=clipboard-copy] a").click();
    await expect(page.getByText("Success")).toBeVisible();
    await page.locator("[data-cy=clipboard-link] a").click();
    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });
});
