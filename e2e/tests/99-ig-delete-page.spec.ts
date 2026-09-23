// Delete Interest Group — renamed 89 -> 99 so it sorts LAST and runs at the very
// end of the suite. The Test IG is shared by every IG-dependent test (40-88x);
// under fullyParallel a delete queued at position 89 could run on the other
// worker while 88g/88h were still using the IG, deleting it mid-test (the IG
// title then vanished from /me/roles). Queuing the delete last means all
// earlier IG-dependent tests are dequeued/finished before it runs.
import { test, expect, env } from "./fixtures";

test.describe("Delete Interest Group", () => {
  test("should delete interest group", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["category.admin.username"], env["category.admin.password"]);

    // Navigate through roles → category → group list → group → admin.
    // The group list can now contain more than one IG (e.g. the imported
    // "HZE Test IG" from 89b), so target the seed IG by its title rather than
    // the first .group-title (which would be a strict-mode violation).
    await page.goto("/me/roles");
    await page.locator('[data-cy="categories"]>.group-title').click();
    await page.locator('[data-cy="group-list"]').click();
    await page
      .locator('[data-cy="groups"]>.group-title')
      .filter({ hasText: env["interest.group.title"] })
      .click();
    await page.locator('[data-cy="admin"]').click();

    // Delete the interest group
    await page.locator('[data-cy="delete-group"]').click();
    await page.locator('[data-cy="verify"]').click();
    await page.locator('[data-cy="delete-button"]').click();

    // Verify success
    await expect(page.getByText("Success")).toBeVisible();
    await page.getByText("Close").click();

    await checkA11yWithLogging();
  });
});
