// Category lifecycle — create category → upload logo → select logo → delete logo.
// Merged from former 22-category-create + 23/24/25 category-logo specs.
//
// Why merged: a category logo cannot be uploaded/selected/deleted unless the
// category exists first. Under fullyParallel the logo steps used to start
// before (or race) the category-create step on another worker and time out
// waiting for "Test Category 1". describe.serial pins the whole chain to one
// worker, in order, and retries it as a group from the category-create step.
import { test, expect, env } from "./fixtures";

test.describe.serial("Category - Lifecycle", () => {
  test("should create category", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    // The user-directory search is Lucene/Solr-backed and therefore eventually
    // consistent, so it may need to be retried while the user that test #10
    // just created gets indexed. Give the test headroom over the 30s default.
    test.setTimeout(90_000);

    // The CircaBC instance (test #20) and header (test #21) are created moments
    // earlier and are eventually consistent (Solr-indexed), so `/admin/headers`
    // may render before the "add category" affordance is available.
    //
    // The "add category" button only renders for a CIRCABC admin
    // (`isCircabcAdmin === 'true'`). That flag is read from the user profile
    // snapshot captured at login time and persisted in sessionStorage; a plain
    // page reload re-applies the same stale snapshot and never picks up the
    // role. Under fullyParallel this test can log in before test #20 has
    // granted CircabcAdmin the CIRCABC-admin role, so we must re-login on every
    // retry to re-fetch the freshly granted profile — reloading alone would
    // loop forever on the pre-grant snapshot. Retry until the button appears.
    await expect(async () => {
      await login(env["circabc.admin.username"], env["circabc.admin.password"]);
      await page.goto("/admin/headers");
      await page.waitForLoadState("networkidle");
      await expect(page.locator('[data-cy="add-category"]')).toBeVisible({
        timeout: 5_000,
      });
    }).toPass({ timeout: 60_000, intervals: [1_000, 2_000, 3_000, 5_000] });

    // Check if category already exists (idempotent)
    const categoryExists = await page
      .getByText(env["category.name"])
      .first()
      .isVisible()
      .catch(() => false);
    if (categoryExists) {
      await checkA11yWithLogging();
      return;
    }

    await page.locator('[data-cy="add-category"]').click();
    await page.locator('[data-cy="name"]').fill(env["category.name"]);
    await page
      .locator('[data-cy="title"]')
      .locator('[data-cy="text"]')
      .fill(env["category.description"]);
    await page.locator('[data-cy="headers"]').selectOption(env["header.name"]);

    await page.locator('[data-cy="admins"]').click();

    // Search for the category admin user. The search is Solr-backed (eventually
    // consistent): the user created moments earlier in test #10 may not be
    // indexed yet, so the first search can return zero results. Re-run the
    // search until the freshly created admin appears.
    const searchBox = page.locator("#name");
    const searchButton = page.locator('[data-cy="search-button"]');
    const userOption = `${env["category.admin.first.name"]} ${env["category.admin.last.name"]} (${env["category.admin.email"]})`;
    const adminOption = page
      .locator('[data-cy="users"]')
      .locator(`option`, { hasText: env["category.admin.first.name"] });

    await expect(async () => {
      await searchBox.fill(env["category.admin.username"]);
      await searchButton.click();
      await expect(adminOption).toBeVisible({ timeout: 5_000 });
    }).toPass({ timeout: 45_000, intervals: [1_000, 2_000, 3_000, 5_000] });

    await page.locator('[data-cy="users"]').selectOption(userOption);
    await page.locator('[data-cy="add-selection"]').click();
    await page.locator('[data-cy="ok"]').click();

    await expect(page.getByText("Success").first())
      .toBeVisible()
      .catch(() => expect(page.getByText(env["category.name"])).toBeVisible());
    await page
      .getByText("Close")
      .click()
      .catch(() => {});

    await checkA11yWithLogging();
  });

  test("should upload category logo", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["category.admin.username"], env["category.admin.password"]);

    // Navigate through explore → header → category → administer
    await page.goto("/explore");
    await page.getByText(env["header.name"] || "Test Header 1").click();
    await page.getByText(env["category.name"] || "Test Category 1").click();
    await page.locator('[data-cy="administer-category"]').click();
    await page.locator('[data-cy="tab-customisation"]').click();
    await page.locator('[data-cy="upload-logo"]').click();

    // Upload logoCategory.png as category logo
    await page
      .locator('[data-cy="file-input"]')
      .setInputFiles("fixtures/files/logoCategory.png");
    await page.locator('[data-cy="ok"]').click();

    // Verify upload success
    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  test("should select category logo as active", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["category.admin.username"], env["category.admin.password"]);

    // Navigate through explore → header → category → administer → customisation
    await page.goto("/explore");
    await page.getByText(env["header.name"] || "Test Header 1").click();
    await page.getByText(env["category.name"] || "Test Category 1").click();
    await page.locator('[data-cy="administer-category"]').click();
    await page.locator('[data-cy="tab-customisation"]').click();

    // Select the first available logo as active
    await page.locator('[data-cy="select-logo"]').first().click();

    // Verify success
    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  test("should delete category logo", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["category.admin.username"], env["category.admin.password"]);

    // Navigate through explore → header → category → administer → customisation
    await page.goto("/explore");
    await page.getByText(env["header.name"] || "Test Header 1").click();
    await page.getByText(env["category.name"] || "Test Category 1").click();
    await page.locator('[data-cy="administer-category"]').click();
    await page.locator('[data-cy="tab-customisation"]').click();

    // Delete the first logo
    await page.locator('[data-cy="delete-inline"]').first().click();
    await page.locator("#confirmDeletePerm").click();

    // Verify success
    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  // Was 26-category-details: display the category detail tabs. Folded into this
  // serial chain because it depends on the category existing and used to race
  // the category-create step under fullyParallel.
  test("should display the category detail tabs", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["category.admin.username"], env["category.admin.password"]);

    // Navigate to the category via /me/roles (CatAdmin1 is a category admin).
    await page.goto("/me/roles");
    await page
      .locator('[data-cy="categories"] .group-title > a')
      .first()
      .click();
    await page.waitForLoadState("networkidle");

    // Lands on the category page (details tab by default).
    await expect(page).toHaveURL(/\/category\/.+\/details/);
    await expect(page.locator(".page-header__title")).toBeVisible();
    await checkA11yWithLogging();

    // Administrators tab
    await page.locator('[data-cy="tab-administrators"]').click();
    await page.waitForLoadState("networkidle");
    await expect(page).toHaveURL(/\/administrators/);
    await checkA11yWithLogging();

    // IG statistics tab
    await page.locator('[data-cy="tab-ig-statistics"]').click();
    await page.waitForLoadState("networkidle");
    await expect(page).toHaveURL(/\/ig-statistics/);
    await checkA11yWithLogging();

    // Support tab
    await page.locator('[data-cy="tab-support"]').click();
    await page.waitForLoadState("networkidle");
    await expect(page).toHaveURL(/\/support/);
    await checkA11yWithLogging();

    // Back to details tab
    await page.locator('[data-cy="tab-details"]').click();
    await page.waitForLoadState("networkidle");
    await expect(page).toHaveURL(/\/details/);
    await checkA11yWithLogging();
  });
});
