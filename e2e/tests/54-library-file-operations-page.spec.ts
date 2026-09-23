// Library file-operations lifecycle — all operations on the single uploaded
// file "CIRCABC_Leader_Guide.pdf" (uploaded by test 52).
// Merged from former 54/55/56a/56b/56c/56d specs.
//
// Why merged + serial: these steps mutate and depend on the same file's
// check-out/lock state, so their order matters:
//   check-out (locks) -> check-in (releases) -> copy check-out (locks again)
//   -> file details (read) -> undo check-out (releases) -> file edit.
// Under fullyParallel they ran as separate files across workers, so check-in
// (55) and copy-check-out (56a) raced the check-out step and flaked.
// describe.serial pins the whole chain to one worker, in order, and retries it
// as a group.
import { test, expect, env } from "./fixtures";

const testFileName = "CIRCABC_Leader_Guide.pdf";

test.describe.serial("Library - File Operations", () => {
  test("should check out file", async ({
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

    // Click on file to open details
    await page.getByText(testFileName).first().click();

    // Click check-out
    await page.locator("[data-cy=checkout]").click();

    // Verify success
    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  test("should check in file", async ({
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

    // Click on file, go to working copy, check in
    await page.getByText(testFileName).first().click();
    await page.getByText("Go to the working copy").click();
    await page.locator("[data-cy=checkin]").click();
    await page.locator('[data-cy="ok"]').click();

    await expect(page.getByText("Success")).toBeVisible();

    // Wait for the lock to be fully released before ending the test
    await expect(page.getByText("Go to the working copy")).toBeHidden({
      timeout: 15000,
    });

    await checkA11yWithLogging();
  });

  test("should check-out in copied IG context", async ({
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

    // Click on file and check out
    await page.getByText(testFileName).first().click();

    // Detect if the file is still locked from a previous test run
    const isLocked = await page
      .getByText("This content is now locked by")
      .isVisible({ timeout: 3000 })
      .catch(() => false);

    // If locked, recover by cancelling the stale checkout first
    if (isLocked) {
      await page.getByText("Go to the working copy").click();
      await page.locator("[data-cy=cancel-checkout]").click();
      await page.locator('[data-cy="ok"]').click();
      await expect(page.getByText("Success")).toBeVisible();

      await navigateToIGSection("library");
      await page.getByText(testFileName).first().click();
    }

    // Proceed with the checkout flow
    await page.locator("[data-cy=checkout]").click();

    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  test("should display the file details page and a versioned view", async ({
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

    // Open the file details.
    await page.getByText(testFileName).first().click();
    await page.waitForLoadState("networkidle");

    // Details page renders with its header and permissions sub-link.
    await expect(page).toHaveURL(/\/details/);
    await expect(page.locator(".file-details")).toBeVisible();
    await expect(page.locator("h1.page-header__title")).toBeVisible();
    await expect(page.locator('[data-cy="permissions"]')).toBeVisible();

    await checkA11yWithLogging();

    // Navigate to the first version via the :nodeId/details/:versionLabel route.
    const detailsUrl = page.url();
    const versionUrl = detailsUrl.replace(
      /\/details(\/[^/]+)?$/,
      "/details/1.0",
    );
    await page.goto(versionUrl);
    await page.waitForLoadState("networkidle");

    // The versioned details view still renders the file details header.
    await expect(page.locator(".file-details")).toBeVisible();
    await expect(page.locator("h1.page-header__title")).toBeVisible();

    await checkA11yWithLogging();
  });

  test("should undo check-out", async ({
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

    // Click on file, go to working copy, cancel checkout
    await page.getByText(testFileName).first().click();
    await page.getByText("Go to the working copy").click();
    await page.locator("[data-cy=cancel-checkout]").click();
    await page.locator('[data-cy="ok"]').click();

    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  test("should edit the file title/description", async ({
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

    // Open the file details, then derive the edit route.
    await page.getByText(testFileName).first().click();
    await page.waitForLoadState("networkidle");
    await expect(page).toHaveURL(/\/details/);

    const editUrl = page.url().replace(/\/details(\/[^/]+)?$/, "/edit");
    await page.goto(editUrl);
    await page.waitForLoadState("networkidle");

    // Edit form renders (General information tab is default).
    await expect(page).toHaveURL(/\/edit/);
    await expect(page.locator("cbc-edit-node #name")).toBeVisible();

    // Update the title (first multilingual text input) and description.
    await page
      .locator('[data-cy="text"]')
      .first()
      .fill("Leader Guide (edited)");
    await page
      .locator('[data-cy="textarea"]')
      .first()
      .locator(".ql-editor")
      .fill("Edited description for E2E");

    // Save all.
    await page
      .locator(".buttons-group .cta")
      .filter({ hasText: /save/i })
      .click();
    await page.waitForLoadState("networkidle");

    // After saving we leave the edit page (back to details).
    await expect(page).not.toHaveURL(/\/edit/, { timeout: 15000 });

    await checkA11yWithLogging();
  });
});
