// Membership lifecycle — invite user → update role → delete membership.
// Merged from former 41/42/43 specs and run with describe.serial so the
// invited "Access" user exists for the update and delete steps, and the
// whole chain retries together from a clean state instead of racing.
import { test, expect, env } from "./fixtures";

test.describe.serial("Membership - Lifecycle", () => {
  test("should invite user to membership", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Navigate to roles page
    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).click();
    await page.locator('[data-cy="members"]').click();

    // Click actions and invite member
    await page.locator('[data-cy="actions"]').click();
    await page.locator('[data-cy="invite-member"]').click();

    // Search for user
    await page.locator('[data-cy="search-user"]').fill(env["access.username"]);
    await page.locator('[data-cy="search-button"]').click();

    // Wait for search results - if no local results, search Central User Database
    const usersDropdown = page.locator('[data-cy="users"]');
    try {
      await expect(usersDropdown.locator("option")).not.toHaveCount(0, {
        timeout: 3000,
      });
    } catch {
      // Search in Central User Database if local search fails
      await page.getByText("Central User Database").click();
      await expect(usersDropdown.locator("option")).not.toHaveCount(0);
    }

    // Select user from dropdown
    const userFullName = `${env["access.first.name"]} ${env["access.last.name"]} (${env["access.email"]})`;
    await usersDropdown.selectOption(userFullName);
    await page.locator('[data-cy="select"]').click();

    await page.waitForTimeout(500);

    // Enable notification toggle
    await page.locator("#cmn-toggle-1").click();

    // Add message
    await page
      .locator('[data-cy="message"]')
      .fill(
        "When I find myself in times of trouble, Mother Mary comes to me Speaking words of wisdom, let it be",
      );

    // Enable second toggle
    await page.locator("#cmn-toggle-2").click();

    // Submit
    await page.locator('[data-cy="ok"]').click();

    // Verify success
    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  test("should update membership role", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Navigate to roles page
    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).click();
    await page.locator('[data-cy="members"]').click();

    // Hover over second member and click change profile
    await page
      .locator(":nth-child(2) > .cell-firstname > .file-name > a")
      .hover();
    await page.getByText("Change profile").click();

    // Select new profile
    await page.locator("#profileName").selectOption("Access");
    await page.locator('[data-cy="ok"]').click();

    await checkA11yWithLogging();
  });

  test("should delete membership", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Navigate to roles page
    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).click();
    await page.locator('[data-cy="members"]').click();

    // Find Access user row and click Remove
    const accessRow = page
      .locator("tr")
      .filter({ hasText: "access@circabc.eu" });
    await accessRow.hover();
    await accessRow.getByText("Remove").click();

    // Confirm deletion
    await page.locator('[data-cy="ok"]').click();

    await checkA11yWithLogging();
  });
});
