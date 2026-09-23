// Test #87: Dynamic Authority - Leader
import { test, expect } from "./fixtures";

const testFileName = "CIRCABC_Leader_Guide.pdf";

// Serial: invite -> upload+cut-inheritance -> verify form a dependent chain.
test.describe.serial("Dynamic Authority - Leader", () => {
  test("should invite user to interest group", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("IGadmin1", "password123");

    // Navigate to IG → Members
    await page.goto("/me/roles");
    await page.getByText("Test IG title").click();
    await page.locator('[data-cy="members"]').click();

    // Invite member
    await page.locator('[data-cy="actions"]').click();
    await page.locator('[data-cy="invite-member"]').click();
    await page.locator('[data-cy="search-user"]').fill("Author");
    await page.locator('[data-cy="search-button"]').click();

    // Wait for search results to populate the listbox
    await expect(page.locator("[data-cy=users] option")).not.toHaveCount(0, {
      timeout: 10000,
    });
    // Select the first option containing "Author"
    const options = page.locator("[data-cy=users] option");
    const count = await options.count();
    for (let i = 0; i < count; i++) {
      const text = await options.nth(i).textContent();
      if (text && text.includes("Author")) {
        await page.locator("[data-cy=users]").selectOption({ index: i });
        break;
      }
    }
    await page.locator("[data-cy=select]").click();
    await page.waitForTimeout(500);

    // Send notification
    await page.locator("#cmn-toggle-1").click();
    await page
      .locator('[data-cy="message"]')
      .fill(
        "You have been invited to the interest group. Please upload a document.",
      );
    await page.locator("#cmn-toggle-2").click();

    await page.locator("[data-cy=ok]").click();
    await expect(
      page.getByLabel("Notifications").getByText("Success"),
    ).toBeVisible();

    // Client-only session reset. Do NOT navigate to /login/logout: Alfresco
    // reuses a single ticket per user, so a server-side logout would expire
    // other parallel IGadmin1/Author tests. The next test's login() re-auths.
    await page.context().clearCookies();
    await page.evaluate(() => sessionStorage.clear());

    await checkA11yWithLogging();
  });

  test("should upload file and cut inheritance", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("Author", "password123");

    // Navigate to IG → Library
    await page.goto("/me/roles");
    await page.getByText("Test IG title").click();
    await page.waitForTimeout(1000);
    await page.locator("[data-cy=library]").click();

    // Upload file
    await page.locator("[data-cy=add]").click();
    await page.locator("[data-cy=files]").click();
    await page
      .locator('[data-cy="file-input"]')
      .setInputFiles("fixtures/files/CIRCABC_Leader_Guide.pdf");
    await page.locator("[data-cy=upload]").click();
    await page.locator("[data-cy=finish]").click();

    // Verify file uploaded
    await expect(page.getByText(testFileName).first()).toBeVisible();

    // Click on file details
    await page.waitForTimeout(1000);
    await page.getByText(testFileName).first().click();

    // Cut inheritance
    await page.waitForTimeout(1000);
    await page.locator('[data-cy="permissions"]').click();
    await page.locator(".nonBlockLabel").click();
    await expect(
      page.getByLabel("Notifications").getByText("Success"),
    ).toBeVisible();

    // Client-only session reset. Do NOT navigate to /login/logout: Alfresco
    // reuses a single ticket per user, so a server-side logout would expire
    // other parallel IGadmin1/Author tests. The next test's login() re-auths.
    await page.context().clearCookies();
    await page.evaluate(() => sessionStorage.clear());

    await checkA11yWithLogging();
  });

  test("should verify admin can still view file details", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("IGadmin1", "password123");

    // Navigate to IG → Library
    await page.goto("/me/roles");
    await page.getByText("Test IG title").click();
    await page.locator("[data-cy=library]").click();

    // Verify file visible
    await expect(page.getByText(testFileName).first()).toBeVisible();

    // Click on file
    await page.getByText(testFileName).first().click();

    // Client-only session reset. Do NOT navigate to /login/logout: Alfresco
    // reuses a single ticket per user, so a server-side logout would expire
    // other parallel IGadmin1/Author tests. The next test's login() re-auths.
    await page.context().clearCookies();
    await page.evaluate(() => sessionStorage.clear());

    await checkA11yWithLogging();
  });
});
