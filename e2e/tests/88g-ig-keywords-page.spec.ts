// Test #88g: Interest Group - Keywords management (CRUD)
//
// Depends on: IG created (test 30). Login as the IG admin (canActivateAdmin).
import { test, expect, env } from "./fixtures";

const KEYWORD = "E2EKeyword";

test.describe("IG - Keywords", () => {
  test("should create and delete a keyword", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Resolve the IG id from the group dashboard, then go to keywords.
    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).first().click();
    // Wait for the SPA to actually navigate to the group route before reading
    // the id (avoids the intermittent `igId === undefined`).
    await page.waitForURL(/\/group\/[^/]+/, { timeout: 15000 });
    await page.waitForLoadState("networkidle");
    const igId = page.url().match(/\/group\/([^/]+)/)?.[1];
    expect(igId).toBeTruthy();
    await page.goto(`/group/${igId}/keywords`);
    await page.waitForLoadState("networkidle");

    await expect(page.locator(".page-header__title")).toBeVisible();

    // Idempotent: if the keyword already exists, remove it first via its row.
    const existing = page
      .locator(".keywordTagValue")
      .filter({ hasText: KEYWORD });
    if (
      await existing
        .first()
        .isVisible()
        .catch(() => false)
    ) {
      await page
        .locator("tr.row")
        .filter({ hasText: KEYWORD })
        .locator("a.delete")
        .first()
        .click();
      await page.locator(".modal-footer .buttons-group .cta").click();
      await page.waitForLoadState("networkidle");
    }

    // Create keyword
    await page.locator('[data-cy="add-keyword"]').click();
    await page.locator('[data-cy="create-keyword"]').click();
    await page.locator('[data-cy="text"]').fill(KEYWORD);
    await page.locator('[data-cy="ok"]').click();
    await page.waitForLoadState("networkidle");

    // Assert it appears in the list
    await expect(
      page.locator(".keywordTagValue").filter({ hasText: KEYWORD }).first(),
    ).toBeVisible({ timeout: 15000 });

    await checkA11yWithLogging();

    // Delete the keyword
    await page
      .locator("tr.row")
      .filter({ hasText: KEYWORD })
      .locator("a.delete")
      .first()
      .click();
    await page.locator(".modal-footer .buttons-group .cta").click();
    await page.waitForLoadState("networkidle");

    await expect(
      page.locator(".keywordTagValue").filter({ hasText: KEYWORD }),
    ).toHaveCount(0, { timeout: 15000 });

    await checkA11yWithLogging();
  });
});
