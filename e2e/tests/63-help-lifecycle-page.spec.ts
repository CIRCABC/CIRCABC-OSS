// Help lifecycle — the whole Help feature as one serial chain.
// Merged from former 63/65/66/67/68/69/70/71/72/72a/73 specs.
//
// Why one file (and not one per create/update/delete group):
// "Section1" is created first and only deleted last, but the help LINK steps
// (create/update/search/delete) and help ARTICLE steps (create/update/
// highlight/delete) all run in between and depend on that section (and on
// each other's state, e.g. search finds "Link1-Updated" before the link is
// deleted). Under fullyParallel, splitting these into separate files let a
// section-delete file race the article file. Keeping the entire lifecycle in
// one describe.serial guarantees order on a single worker and group retries.
//
// Order preserved from the original numeric prefixes:
//   63 section-create → 65 link-create → 66 link-update → 67 search
//   → 68 link-delete → 69 article-create → 70 article-update
//   → 71 article-highlight → 72 article-delete → 72a section-update
//   → 73 section-delete
import { test, expect, env } from "./fixtures";

test.describe.serial("Help - Lifecycle", () => {
  // --- Section create (was 63) ---
  test("should create help section", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["admin.username"], env["admin.password"]);

    await page.goto("/help/start");

    // Check if section already exists (idempotent)
    const sectionExists = await page
      .getByText("Section1")
      .first()
      .isVisible()
      .catch(() => false);
    if (sectionExists) {
      await checkA11yWithLogging();
      return;
    }

    await page.getByText("Add section").click();
    await page
      .getByRole("textbox", { name: "Title" })
      .pressSequentially("Section1");
    await page.getByText("Create").first().click();

    await expect(page.getByText("Section created")).toBeVisible();

    await checkA11yWithLogging();
  });

  // --- Link create (was 65) ---
  test("should create help link", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("CircabcAdmin", "password123");

    // Navigate to help section
    await page.goto("/help/start");

    // Create link
    await page.locator("header.actions > :nth-child(1)").click();
    await page.locator('[data-cy="text"]').fill("Link1");
    await page.locator("#href").fill("http://link1.test");
    await page.locator('[data-cy="ok"]').click();

    // Verify success
    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  // --- Link update (was 66) ---
  test("should update help link", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("CircabcAdmin", "password123");

    // Navigate to help section
    await page.goto("/help/start");

    // Edit link
    await page.locator('[data-cy="edit-inline"]').click();
    await page.locator('[data-cy="text"]').fill("Link1-Updated");
    await page.locator("#href").fill("http://link1-updated.test");
    await page.locator('[data-cy="ok"]').click();

    // Verify success
    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  // --- Search (was 67) — finds the updated link, before it is deleted ---
  test("should search help content", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["admin.username"], env["admin.password"]);

    await page.goto("/help/start");

    // The help page search bar has a different placeholder
    const searchBox = page.getByRole("textbox", { name: "Search" });
    await searchBox.click();
    await searchBox.fill("Link1");

    // The link was renamed to "Link1-Updated" by a previous test, search for it
    await expect(page.getByText("Link1-Updated")).toBeVisible({
      timeout: 20000,
    });

    await checkA11yWithLogging();
  });

  // --- Link delete (was 68) ---
  test("should delete help link", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("CircabcAdmin", "password123");

    // Navigate to help section
    await page.goto("/help/start");

    // Delete link
    await page.locator('[data-cy="delete-inline"]').first().click();
    await page.locator("#confirmDeletePerm").click();

    // Verify success
    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  // --- Article create (was 69) ---
  test("should create help article", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("CircabcAdmin", "password123");

    // Navigate to help section
    await page.goto("/help/start");
    await expect(page.getByText("Section1")).toBeVisible();
    await page.locator("[data-cy=category-link]").first().click();

    // Create article
    await page.locator("[data-cy=add-article]").click();
    await page.locator('[data-cy="text"]').fill("Article1");
    await page
      .locator(".field > .custom-select > .ng-valid")
      .selectOption("en");
    await page.locator(".ql-editor").fill("Space 1 Description");
    await page.locator('[data-cy="ok"]').click();

    // Verify success
    await expect(page.getByText("Article created")).toBeVisible();

    await checkA11yWithLogging();
  });

  // --- Article update (was 70) ---
  test("should update help article", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("CircabcAdmin", "password123");

    // Navigate to help section
    await page.goto("/help/start");
    await expect(page.getByText("Section1")).toBeVisible();
    await page.locator("[data-cy=category-link]").first().click();
    await page.locator("[data-cy=article-link]").first().click();

    // Update article
    await page.locator("[data-cy=edit-article]").click();
    await page.locator('[data-cy="text"]').fill(" updated");
    await page
      .locator(
        ".modal-content > form.ng-untouched > .field > .custom-select > .ng-untouched",
      )
      .selectOption("es");
    await page.locator(".ql-editor").fill(" updated");
    await page.locator('[data-cy="ok"]').click();

    // Verify success
    await expect(page.getByText("Article updated")).toBeVisible();

    await checkA11yWithLogging();
  });

  // --- Article highlight (was 71) ---
  test("should highlight help article", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("CircabcAdmin", "password123");

    // Navigate to help section
    await page.goto("/help/start");
    await expect(page.getByText("Section1")).toBeVisible();
    await page.locator("[data-cy=category-link]").first().click();
    await page.locator("[data-cy=article-link]").first().click();

    // Toggle highlight
    await page.locator("[data-cy=toggle-highlight]").click();

    // Verify success
    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  // --- Article delete (was 72) ---
  test("should delete help article", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("CircabcAdmin", "password123");

    // Navigate to help section
    await page.goto("/help/start");
    await expect(page.getByText("Section1")).toBeVisible();
    await page.locator("[data-cy=category-link]").first().click();
    await page.locator("[data-cy=article-link]").first().click();

    // Delete article
    await page.locator("[data-cy=delete-article]").click();
    await page.locator('[data-cy="ok"]').click();

    // Verify success
    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  // --- Section update (was 72a) ---
  test("should update help section", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["admin.username"], env["admin.password"]);

    await page.goto("/help/start");
    await page.waitForTimeout(5000);

    await page.locator(".help-categories > ul > li > a").first().click();
    await page.locator(".article-list > .actions > :nth-child(1)").click();
    await page.locator('[data-cy="text"]').fill(" updated");
    await page.locator('[data-cy="ok"]').click();

    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  // --- Section delete (was 73) ---
  test("should delete help section", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["admin.username"], env["admin.password"]);

    await page.goto("/help/start");
    await page
      .locator(".help-categories > ul > :nth-child(1) > a")
      .waitFor({ state: "visible" });

    await page.locator(".help-categories > ul > :nth-child(1) > a").click();
    await page.locator(".selected > a").click();
    await page.locator(".actions > :nth-child(2)").click();
    await page.locator('[data-cy="ok"]').click();

    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });
});
