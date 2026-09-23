// IG membership-request lifecycle — create request → approve → request deletion
// → approve deletion. Merged from former 35/36/37/38 specs.
//
// Why merged + serial: each "approve" step needs the matching "request" step to
// have completed first. Under fullyParallel the approve specs raced ahead of the
// request specs on the other worker (hence the flaky "IG Request - Approve
// Create"). describe.serial pins the four steps to one worker in order and
// retries the whole chain as a group. They were already consecutive by numeric
// prefix (34-FTP in between is skipped), so global ordering is unchanged.
import { test, expect, env } from "./fixtures";

test.describe.serial("IG Request - Lifecycle", () => {
  test("should create membership request", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Navigate to explore and request new IG creation
    await page.goto("/explore");

    // Click on links to navigate
    await page.locator(".link").first().click();
    await page.locator(".link").first().click();
    await page.waitForTimeout(2000);

    // Click dropdown and request group
    await page.locator("cbc-explorer-dropdown > .cta").click();
    await page.locator(".request--group").click();

    // Fill form
    await page.locator("#header").selectOption({ index: 0 });
    await page.waitForTimeout(1000);
    await page.locator("#category").selectOption({ index: 0 });
    await page.waitForTimeout(1000);

    // Fill name and title
    await page
      .locator(":nth-child(4) > :nth-child(1) > .ng-untouched")
      .fill("aaaa");
    await page
      .locator(":nth-child(4) > :nth-child(2) > .ng-pristine")
      .fill("aaaa");
    await page.waitForTimeout(1000);

    // Fill comment
    await page
      .locator("#comment")
      .fill(
        "Ah, ha, ha, ha, stayin' alive, stayin' alive ..  stayin' aliiiiiiive",
      );

    // Submit
    await page.locator(".cta").click();
    await page.locator(".cta").click();

    // Verify success
    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  test("should approve membership request", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["category.admin.username"], env["category.admin.password"]);

    // Navigate to category administration
    await page.goto("/explore");
    await page.getByText(env["header.name"] || "Test Header 1").click();
    await page.getByText(env["category.name"] || "Test Category 1").click();
    await page.locator('[data-cy="administer-category"]').click();
    await page.locator('[data-cy="tab-group-requests"]').click();
    await page.waitForTimeout(1000);

    // Approve the first request
    await page.locator('[data-cy="approve"]').first().click();
    await page.waitForTimeout(1000);

    // Add approval comment
    await page
      .locator(".ql-editor")
      .fill(
        "Ah, ha, ha, ha, stayin' alive, stayin' alive ..  stayin' aliiiiiiive",
      );
    await page.getByRole("dialog").getByText("Send").click();

    // Verify success
    await expect(page.getByText("Success").first()).toBeVisible();

    await checkA11yWithLogging();
  });

  test("should request membership deletion", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Navigate to roles and select IG
    await page.goto("/me/roles");
    await page.locator(":nth-child(2) > .group-title").click();
    await page.locator('[data-cy="admin"]').click();

    // Request IG deletion
    await page.locator(".page-header .cta.dropdown-trigger").click();
    const deleteRequestLink = page.locator('[data-cy="igReqDelete"]');
    await expect(deleteRequestLink).toBeVisible();
    await deleteRequestLink.click();
    await page
      .locator('[data-cy="justification"]')
      .type("Request Delete IG Page e2e ");
    await page.locator('[data-cy="ok"]').click();

    // Verify success
    await expect(
      page.getByText("Request successfully submitted"),
    ).toBeVisible();

    await checkA11yWithLogging();
  });

  test("should approve membership deletion request", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    // This test depends on the previous "request membership deletion" step.
    await login(env["category.admin.username"], env["category.admin.password"]);

    // Navigate to category administration
    await page.goto("/explore");
    await page.getByText(env["header.name"] || "Test Header 1").click();
    await page.getByText(env["category.name"] || "Test Category 1").click();
    await page.locator('[data-cy="administer-category"]').click();
    await page.locator('[data-cy="tab-group-requests"]').click();
    await page.waitForTimeout(1000);

    // Go to delete requests tab
    await page.locator('[data-cy="tab-delete-requests"]').click();
    await page.locator('[data-cy="approve"]').first().click();
    await page.waitForTimeout(1000);
    await page.locator('[data-cy="delete-button"]').click();

    // Verify success
    await expect(
      page.getByText("Request successfully submitted"),
    ).toBeVisible();

    await checkA11yWithLogging();
  });
});
