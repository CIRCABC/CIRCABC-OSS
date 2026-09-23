// Test #76: Topic - Create
import { test, expect, env } from "./fixtures";

test.describe("Topic - Create", () => {
  test("should create topic", async ({ page, login, checkA11yWithLogging }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).click();
    await page.locator("[data-cy=forums]").click();

    // Check if topic already exists (idempotent)
    const topicExists = await page
      .getByText("Topic1")
      .first()
      .isVisible()
      .catch(() => false);
    if (topicExists) {
      await checkA11yWithLogging();
      return;
    }

    await page.locator("[data-cy=add]").click();
    await page.locator("[data-cy=create-topic]").click();
    await page.locator('[data-cy="text"]').fill("Topic1");
    await page.locator(".ql-editor").click();
    await page.locator(".ql-editor").fill("Topic1 Description");
    await page.locator('[data-cy="ok"]').click();

    await checkA11yWithLogging();
  });
});
