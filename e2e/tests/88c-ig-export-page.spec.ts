// Test: Export Interest Group (before delete)
import { test, expect, env } from "./fixtures";
import * as fs from "fs";
import * as path from "path";

const EXPORT_FILE = path.join(
  __dirname,
  "..",
  "fixtures",
  "files",
  "ig-export.xml",
);

test.describe("IG Export", () => {
  test("should export interest group via support page", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(env["admin.username"], env["admin.password"]);

    await page.goto("/support/ig-export");
    await expect(page.locator("h3")).toBeVisible();

    // Step 1: Select header via mat-select
    const firstSelect = page.locator("mat-select").first();
    await expect(firstSelect).toBeVisible();
    await firstSelect.click();
    await page.getByRole("option", { name: env["header.name"] }).click();

    // Step 2: Select category via mat-select.
    // The dropdown displays the category's *technical* name (Category.name),
    // which CircaBC derives from the category admin ("CatAdmin1"), not the
    // display title ("Test Category 1"). Only one category exists under the
    // test header, so select the single available option.
    const secondSelect = page.locator("mat-select").nth(1);
    await expect(secondSelect).toBeVisible();
    await secondSelect.click();
    await page.getByRole("option").first().click();

    // Step 3: Select IG via mat-select (wait for loading to finish)
    const thirdSelect = page.locator("mat-select").nth(2);
    await expect(thirdSelect).toBeVisible({ timeout: 15000 });
    await thirdSelect.click();
    await page
      .getByRole("option", { name: env["interest.group.name"], exact: true })
      .click();

    // Trigger export
    const [download] = await Promise.all([
      page.waitForEvent("download"),
      page.locator("button.export-btn").click(),
    ]);

    // Save the exported file for later import test
    await download.saveAs(EXPORT_FILE);
    expect(fs.existsSync(EXPORT_FILE)).toBe(true);

    const content = fs.readFileSync(EXPORT_FILE, "utf-8");
    expect(content).toContain("<?xml");
    expect(content).toContain("importRoot");

    await checkA11yWithLogging();
  });
});
