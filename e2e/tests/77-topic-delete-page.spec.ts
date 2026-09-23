// Test #77: Topic - Delete
import { test, expect, env } from "./fixtures";

test.describe("Topic - Delete", () => {
  test("should delete topic", async ({ page, login, checkA11yWithLogging }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).click();
    await page.locator("[data-cy=forums]").click();

    await page.getByText("Topic1").hover();
    await page.getByText("Delete").click();
    await page.locator(".cta").getByText("Delete").click();

    await expect(page.getByText("Success")).toBeVisible();
    await page.getByText("Close").click();

    await checkA11yWithLogging();
  });
});
