// Test #13: Create Interest Group
import { test, expect, env } from "./fixtures";

test.describe("Create Interest Group", () => {
  test("should create interest group", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    // IG creation depends on the category + CatAdmin1 created moments earlier,
    // whose Solr indexing is eventually consistent; allow headroom for retries.
    test.setTimeout(90_000);

    await login(env["category.admin.username"], env["category.admin.password"]);

    // Navigate to roles and click on group
    await page.goto("/me/roles");

    // Check if IG already exists (idempotent)
    const igExists = await page
      .getByText(env["interest.group.title"])
      .first()
      .isVisible()
      .catch(() => false);
    if (igExists) {
      await checkA11yWithLogging();
      return; // Skip - IG already exists
    }

    // The category (and CatAdmin1) were created moments earlier and are
    // Solr-indexed eventually, so the category may be missing on the first
    // /me/roles render. Retry the reload until the category link appears, then
    // open it and confirm the create form is reachable — this waits until the
    // category is fully created before attempting IG creation.
    const categoryLink = page
      .locator('[data-cy="categories"] .group-title > a')
      .filter({ hasText: env["category.name"] })
      .first();

    await expect(async () => {
      await page.goto("/me/roles");
      await page.waitForLoadState("networkidle");
      await expect(categoryLink).toBeVisible({ timeout: 5000 });
      await categoryLink.click({ timeout: 5000 });
      await expect(page.locator('[data-cy="add-interest-group"]')).toBeVisible({
        timeout: 5000,
      });
    }).toPass({ timeout: 60000, intervals: [1000, 2000, 3000, 5000] });

    // Click create interest group
    await page.locator('[data-cy="add-interest-group"]').click();

    // Fill form
    await page.locator('[data-cy="name"]').fill(env["interest.group.name"]);
    await page
      .locator('[data-cy="title"]>.flexContainer>.flexWrap>[data-cy="text"]')
      .fill(env["interest.group.title"]);
    await page
      .locator('[data-cy="textarea"]')
      .first()
      .locator(".ql-editor")
      .fill(env["interest.group.description"]);
    await page
      .locator('[data-cy="textarea"]')
      .last()
      .locator(".ql-editor")
      .fill(env["interest.group.contact"]);

    await page.locator('[data-cy="ok"]').click();

    // Add admin
    await page
      .locator('[data-cy="search"]')
      .fill(env["interest.group.admin.username"]);
    await page.locator('[data-cy="search-button"]').click();

    const userOption = `${env["interest.group.admin.first.name"]} ${env["interest.group.admin.last.name"]} (${env["interest.group.admin.email"]})`;
    await page.locator('[data-cy="users"]').selectOption(userOption);
    await page.locator('[data-cy="select"]').click();
    await page.locator('[data-cy="ok"]').click();
    await page.locator('[data-cy="ok"]').click();

    // Verify success (IG creation can be slow)
    await expect(page.getByText("successfully created")).toBeVisible({
      timeout: 30000,
    });
    await page.getByText("Close").click();

    await checkA11yWithLogging();
  });
});
