// Test #31: Library - Import ZIP
import { test, expect, env } from "./fixtures";

test.describe("Library - Import ZIP", () => {
  test("should import ZIP file", async ({
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

    // Import ZIP
    await page.locator("[data-cy=add]").click();
    await page.locator(".import").click();
    await page
      .locator('[id="file"]')
      .setInputFiles("fixtures/files/ImportZipTest.zip");
    await page.locator(".modal-footer > .buttons-group > .cta").click();

    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });
});
