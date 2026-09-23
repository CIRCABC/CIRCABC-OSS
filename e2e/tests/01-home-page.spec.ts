// Test #1: Home Page - anonymous user
import { test, expect } from "./fixtures";

test.describe("Home Page", () => {
  test("should display CIRCABC text for anonymous users", async ({
    page,
    checkA11yWithLogging,
  }) => {
    await page.goto("/welcome");
    await expect(
      page.getByRole("heading", { name: "CIRCABC provides a secured" }),
    ).toBeVisible();
    await checkA11yWithLogging();
  });
});
