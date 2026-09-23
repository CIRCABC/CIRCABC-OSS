// Test #88h: Interest Group - Dynamic properties (CRUD)
//
// Depends on: IG created (test 30). Login as the IG admin (canActivateAdmin).
import { test, expect, env } from "./fixtures";

const DYNPROP = "E2EDynProp";

test.describe("IG - Dynamic Properties", () => {
  test("should create and delete a dynamic property", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Resolve the IG id from the group dashboard, then go to dynamic-properties.
    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).first().click();
    // Wait for the SPA to actually navigate to the group route before reading
    // the id (avoids the intermittent `igId === undefined`).
    await page.waitForURL(/\/group\/[^/]+/, { timeout: 15000 });
    await page.waitForLoadState("networkidle");
    const igId = page.url().match(/\/group\/([^/]+)/)?.[1];
    expect(igId).toBeTruthy();
    await page.goto(`/group/${igId}/dynamic-properties`);
    await page.waitForLoadState("networkidle");

    await expect(page.locator(".page-header__title")).toBeVisible();

    // Idempotent: delete an existing property with this title first.
    const existing = page
      .locator("cbc-dynamic-property-box")
      .filter({ hasText: DYNPROP });
    if (
      await existing
        .first()
        .isVisible()
        .catch(() => false)
    ) {
      await existing
        .first()
        .locator(".actions a", { hasText: "Delete" })
        .click();
      await page
        .locator(".modal-footer .buttons-group .cta, .modal .cta")
        .first()
        .click();
      await page.waitForLoadState("networkidle");
    }

    // Create a TEXT dynamic property
    await page.locator('[data-cy="add-dynamic-property"]').click();
    await page.locator("#propertyType").selectOption("TEXT_FIELD");
    await page.locator('[data-cy="text"]').fill(DYNPROP);
    await page.locator('[data-cy="ok"]').click();
    await page.waitForLoadState("networkidle");

    // Assert it appears
    await expect(
      page
        .locator("cbc-dynamic-property-box")
        .filter({ hasText: DYNPROP })
        .first(),
    ).toBeVisible({ timeout: 15000 });

    await checkA11yWithLogging();

    // Delete it
    await page
      .locator("cbc-dynamic-property-box")
      .filter({ hasText: DYNPROP })
      .first()
      .locator(".actions a")
      .filter({ hasText: "Delete" })
      .click();
    await page
      .locator(".modal-footer .buttons-group .cta, .modal .cta")
      .first()
      .click();
    await page.waitForLoadState("networkidle");

    await expect(
      page.locator("cbc-dynamic-property-box").filter({ hasText: DYNPROP }),
    ).toHaveCount(0, { timeout: 15000 });

    await checkA11yWithLogging();
  });
});
