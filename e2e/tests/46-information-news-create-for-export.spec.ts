// Test: Create information news
import { test, expect, env } from "./fixtures";

test.describe("Information News - Create for Export", () => {
  test("should create text news in information service", async ({
    page,
    login,
    navigateToIGSection,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    await navigateToIGSection("information");

    // Check if news already exists
    const newsExists = await page
      .getByText("Export Test News")
      .first()
      .isVisible()
      .catch(() => false);
    if (newsExists) return;

    // Click Add news
    await page.locator('[data-cy="add-news"]').click();
    await page.waitForLoadState("networkidle");

    // Fill the title
    await page.getByRole("textbox", { name: "Title" }).fill("Export Test News");

    // Fill the content (rich text editor)
    await page.locator(".ql-editor").click();
    await page.locator(".ql-editor").fill("News content for export test");

    // Save
    await page.locator('[data-cy="save-news"]').click();

    await expect(page.getByText("Export Test News")).toBeVisible({
      timeout: 15000,
    });
  });
});
