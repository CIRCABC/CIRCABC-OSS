// Test #26: Library - Upload
import { test, expect, env } from "./fixtures";

// Serial: "uploaded file visible in user dashboard" depends on the upload step.
test.describe.serial("Library - Upload", () => {
  test("should upload file to library", async ({
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

    // Now upload file
    await page.locator('[data-cy="add"]').click();
    await page.locator('[data-cy="files"]').click();
    await page
      .locator('[data-cy="file-input"]')
      .setInputFiles("fixtures/files/CIRCABC_Leader_Guide.pdf");
    await page.locator('[data-cy="upload"]').click();
    await page.locator('[data-cy="finish"]').click();

    await checkA11yWithLogging();
  });

  test("uploaded file visible in user dashboard", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    await page.goto("/me");

    // Wait longer for dashboard to fully load
    await page.waitForTimeout(3000);
    await page.waitForLoadState("domcontentloaded");

    // Click on the specific Uploads link element
    await page.locator('a:has-text("Uploads ")').click();

    await expect(
      page.getByText("CIRCABC_Leader_Guide.pdf").first(),
    ).toBeVisible();

    await checkA11yWithLogging();
  });
});
