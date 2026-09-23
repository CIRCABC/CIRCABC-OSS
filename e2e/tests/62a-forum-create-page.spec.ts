// Test #38: Forum - Create
import { test, expect, env } from "./fixtures";

test.describe("Forum - Create", () => {
  test("should create forum", async ({ page, login, checkA11yWithLogging }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).click();
    await page.locator("[data-cy=forums]").waitFor({ state: "visible" });
    await page.locator("[data-cy=forums]").click();

    // Check if forum already exists (idempotent)
    const forumExists = await page
      .getByText("Forum1")
      .first()
      .isVisible()
      .catch(() => false);
    if (forumExists) {
      await checkA11yWithLogging();
      return;
    }

    await page.locator("[data-cy=add]").click();
    await page.locator("[data-cy=create-forum]").click();
    await page.locator('[data-cy="text"]').fill("Forum1");
    await page
      .locator('[data-cy="textarea"] .ql-editor')
      .fill("Forum1 Description");
    await page.locator('[data-cy="ok"]').click();

    await checkA11yWithLogging();
  });
});
