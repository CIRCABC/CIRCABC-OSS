// Test #27: Library - Download
import { test, expect, env } from "./fixtures";

// Serial: "downloaded file visible in user dashboard" depends on the download step.
test.describe.serial("Library - Download", () => {
  test("should download file", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("IGadmin1", "password123");

    // Navigate to "Test IG" → Library
    await page.goto("/me/roles");
    await page.getByText("Test IG title").click();
    await page.locator('[data-cy="library"]').click();

    // Download file
    await page
      .getByRole("row", { name: /CIRCABC_Leader_Guide\.pdf/ })
      .getByText("Download")
      .click();
    await page.locator('[data-cy="download"]').click();

    await checkA11yWithLogging();
  });

  test("downloaded file visible in user dashboard", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("IGadmin1", "password123");

    await page.goto("/me");
    await page.getByText("Downloads").click();
    await expect(
      page.getByText("CIRCABC_Leader_Guide.pdf").first(),
    ).toBeVisible();

    await checkA11yWithLogging();
  });
});
