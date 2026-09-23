// Test: Information Service - Create & Delete News (all 5 types)
import { test, expect, env } from "./fixtures";

// Run the whole file serially on one worker: the "Delete News" block depends on
// the items created by the "Create News" block, so under fullyParallel they
// raced across workers (delete started before create finished -> interrupted /
// flaky). File-level serial keeps create-then-delete order and retries the
// whole file as a group.
test.describe.configure({ mode: "serial" });

test.describe("Information Service - Create News", () => {
  test.beforeEach(async ({ page, login }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Navigate to IG information section
    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).click();
    await page.locator('[data-cy="information"]').waitFor({ state: "visible" });
    await page.locator('[data-cy="information"]').click();
    await page.waitForLoadState("networkidle");
  });

  test("should create text news", async ({ page, checkA11yWithLogging }) => {
    // Click "Add" link (transloco key: information.add.news => "Add")
    await page.locator("a.cta").filter({ hasText: "Add" }).click();
    await page.waitForLoadState("networkidle");

    // Type: text (default, no need to change)

    // Fill title
    await page.getByRole("textbox", { name: "Title" }).fill("Text News Title");

    // Fill content in Quill editor
    await page.locator(".ql-editor").click();
    await page.locator(".ql-editor").fill("This is a text news content.");

    // Click Create
    await page.locator(".buttons-group a.cta").click();

    // Verify we're back on information page and news is visible
    await expect(page.getByText("Text News Title")).toBeVisible({
      timeout: 15000,
    });

    await checkA11yWithLogging();
  });

  test("should create image news", async ({ page, checkA11yWithLogging }) => {
    await page.locator("a.cta").filter({ hasText: "Add" }).click();
    await page.waitForLoadState("networkidle");

    // Type: image
    await page.locator("#pattern").selectOption("image");

    // Upload image file
    await page
      .locator('input[type="file"]#file')
      .setInputFiles("fixtures/files/logoCategory.png");

    // Fill title
    await page.getByRole("textbox", { name: "Title" }).fill("Image News Title");

    // Fill content
    await page.locator(".ql-editor").click();
    await page.locator(".ql-editor").fill("This is an image news content.");

    // Click Create
    await page.locator(".buttons-group a.cta").click();

    await expect(page.getByText("Image News Title")).toBeVisible({
      timeout: 15000,
    });

    await checkA11yWithLogging();
  });

  test("should create document news", async ({
    page,
    checkA11yWithLogging,
  }) => {
    await page.locator("a.cta").filter({ hasText: "Add" }).click();
    await page.waitForLoadState("networkidle");

    // Type: document
    await page.locator("#pattern").selectOption("document");

    // Upload document file
    await page
      .locator('input[type="file"]#file')
      .setInputFiles("fixtures/files/CIRCABC_Leader_Guide.pdf");

    // Fill title
    await page
      .getByRole("textbox", { name: "Title" })
      .fill("Document News Title");

    // Fill content
    await page.locator(".ql-editor").click();
    await page.locator(".ql-editor").fill("This is a document news content.");

    // Click Create
    await page.locator(".buttons-group a.cta").click();

    await expect(page.getByText("Document News Title")).toBeVisible({
      timeout: 15000,
    });

    await checkA11yWithLogging();
  });

  test("should create date news", async ({ page, checkA11yWithLogging }) => {
    await page.locator("a.cta").filter({ hasText: "Add" }).click();
    await page.waitForLoadState("networkidle");

    // Type: date
    await page.locator("#pattern").selectOption("date");

    // Fill title
    await page.getByRole("textbox", { name: "Title" }).fill("Date News Title");

    // Fill content
    await page.locator(".ql-editor").click();
    await page.locator(".ql-editor").fill("This is a date news content.");

    // Click Create
    await page.locator(".buttons-group a.cta").click();

    await expect(page.getByText("Date News Title")).toBeVisible({
      timeout: 15000,
    });

    await checkA11yWithLogging();
  });

  test("should create iframe news", async ({ page, checkA11yWithLogging }) => {
    await page.locator("a.cta").filter({ hasText: "Add" }).click();
    await page.waitForLoadState("networkidle");

    // Type: iframe
    await page.locator("#pattern").selectOption("iframe");

    // Fill URL (no title/content for iframe)
    await page.locator("#url").fill("https://www.example.com");

    // Click Create
    await page.locator(".buttons-group a.cta").click();

    // iframe shows URL as title in the news list
    await expect(page.getByText("https://www.example.com")).toBeVisible({
      timeout: 15000,
    });

    await checkA11yWithLogging();
  });
});

test.describe("Information Service - Delete News", () => {
  test.beforeEach(async ({ page, login }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Navigate to IG information section
    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).click();
    await page.locator('[data-cy="information"]').waitFor({ state: "visible" });
    await page.locator('[data-cy="information"]').click();
    await page.waitForLoadState("networkidle");

    // Increase viewport to prevent footer from blocking delete button
    await page.setViewportSize({ width: 1920, height: 1400 });
  });

  test("should delete iframe news", async ({ page, checkA11yWithLogging }) => {
    // Click on iframe news in sidebar to highlight it (shows delete action in card)
    await page
      .locator("button.news-item-btn")
      .filter({ hasText: "https://www.example.com" })
      .first()
      .click();
    await page.waitForLoadState("networkidle");

    // Delete via inline-delete
    await page
      .locator('.news-card.highlighted [data-cy="delete-inline"]')
      .click();
    await page
      .locator('.news-card.highlighted [data-cy="delete-inline-confirm"]')
      .click();

    await page.waitForLoadState("networkidle");
    await expect(
      page
        .locator("button.news-item-btn")
        .filter({ hasText: "https://www.example.com" }),
    ).toHaveCount(0, { timeout: 15000 });

    await checkA11yWithLogging();
  });

  test("should delete date news", async ({ page, checkA11yWithLogging }) => {
    await page
      .locator("button.news-item-btn")
      .filter({ hasText: "Date News Title" })
      .first()
      .click();
    await page.waitForLoadState("networkidle");

    await page
      .locator('.news-card.highlighted [data-cy="delete-inline"]')
      .click();
    await page
      .locator('.news-card.highlighted [data-cy="delete-inline-confirm"]')
      .click();

    await page.waitForLoadState("networkidle");
    await expect(
      page
        .locator("button.news-item-btn")
        .filter({ hasText: "Date News Title" }),
    ).toHaveCount(0, { timeout: 15000 });

    await checkA11yWithLogging();
  });

  test("should delete document news", async ({
    page,
    checkA11yWithLogging,
  }) => {
    await page
      .locator("button.news-item-btn")
      .filter({ hasText: "Document News Title" })
      .first()
      .click();
    await page.waitForLoadState("networkidle");

    await page
      .locator('.news-card.highlighted [data-cy="delete-inline"]')
      .click();
    await page
      .locator('.news-card.highlighted [data-cy="delete-inline-confirm"]')
      .click();

    await page.waitForLoadState("networkidle");
    await expect(
      page
        .locator("button.news-item-btn")
        .filter({ hasText: "Document News Title" }),
    ).toHaveCount(0, { timeout: 15000 });

    await checkA11yWithLogging();
  });

  test("should delete image news", async ({ page, checkA11yWithLogging }) => {
    await page
      .locator("button.news-item-btn")
      .filter({ hasText: "Image News Title" })
      .first()
      .click();
    await page.waitForLoadState("networkidle");

    await page
      .locator('.news-card.highlighted [data-cy="delete-inline"]')
      .click();
    await page
      .locator('.news-card.highlighted [data-cy="delete-inline-confirm"]')
      .click();

    await page.waitForLoadState("networkidle");
    await expect(
      page
        .locator("button.news-item-btn")
        .filter({ hasText: "Image News Title" }),
    ).toHaveCount(0, { timeout: 15000 });

    await checkA11yWithLogging();
  });

  test("should delete text news", async ({ page, checkA11yWithLogging }) => {
    await page
      .locator("button.news-item-btn")
      .filter({ hasText: "Text News Title" })
      .first()
      .click();
    await page.waitForLoadState("networkidle");

    await page
      .locator('.news-card.highlighted [data-cy="delete-inline"]')
      .click();
    await page
      .locator('.news-card.highlighted [data-cy="delete-inline-confirm"]')
      .click();

    await page.waitForLoadState("networkidle");
    await expect(
      page
        .locator("button.news-item-btn")
        .filter({ hasText: "Text News Title" }),
    ).toHaveCount(0, { timeout: 15000 });

    await checkA11yWithLogging();
  });
});
