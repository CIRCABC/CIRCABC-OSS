// Test #88: Dynamic Authority - Owner
import { test, expect, env } from "./fixtures";
import { login as apiLogin } from "./fixtures";

const testFileName = "CIRCABC_Leader_Guide.pdf";
let fileDetailsUrl: string;

// Serial: invite contributor -> upload as contributor -> unsubscribe -> verify.
// This was the original smoking-gun flake: under fullyParallel the "upload"
// retry ran after "unsubscribe" had already removed the contributor, so the IG
// was gone from /me/roles. Serial keeps order and retries the chain as a group.
test.describe.serial("Dynamic Authority - Owner", () => {
  test("should invite contributor to interest group", async ({
    page,
    request,
    checkA11yWithLogging,
  }) => {
    // Login as IG admin via API
    await apiLogin(page, request, "IGadmin1", "password123");

    await page.goto("/me/roles");
    await page.getByText("Test IG title").click();
    await page.locator('[data-cy="members"]').click();

    // Wait for members table to load
    await expect(page.locator("table tr").first()).toBeVisible({
      timeout: 10000,
    });
    await page.waitForTimeout(500);

    // Check if Contributor is already a member
    const alreadyMember =
      (await page
        .locator("tr")
        .filter({ hasText: "contributor@circabc.eu" })
        .count()) > 0;
    if (alreadyMember) {
      // Already invited (e.g. by forum moderation test), skip.
      // Client-only session reset (avoid server-side logout that would expire
      // the shared per-user Alfresco ticket used by parallel tests).
      await page.context().clearCookies();
      await page.evaluate(() => sessionStorage.clear());
      await checkA11yWithLogging();
      return;
    }

    await page.locator('[data-cy="actions"]').click();
    await page.locator('[data-cy="invite-member"]').click();
    await page.locator('[data-cy="search-user"]').fill("Contributor");
    await page.locator('[data-cy="search-button"]').click();

    // Wait for search results
    await expect(
      page.locator("[data-cy=users] option:not(:disabled)"),
    ).not.toHaveCount(0, { timeout: 10000 });
    const option = await page
      .locator("[data-cy=users] option:not(:disabled)")
      .first()
      .textContent();
    await page
      .locator("[data-cy=users]")
      .selectOption({ label: option!.trim() });
    await page.locator("[data-cy=select]").click();
    await page.waitForTimeout(500);

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

    // Client-only session reset (avoid server-side logout that would expire the
    // shared per-user Alfresco ticket used by parallel tests).
    await page.context().clearCookies();
    await page.evaluate(() => sessionStorage.clear());

    await checkA11yWithLogging();
  });

  test("should upload file as contributor", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("Contributor", "password123");

    // Navigate to IG → Library
    await page.goto("/me/roles");
    await page.getByText("Test IG title").click();
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

    // Click on file and save URL
    await page.getByText(testFileName).first().click();
    fileDetailsUrl = page.url() + "/details";

    // Logout
    // Client-only session reset (avoid server-side logout that would expire the
    // shared per-user Alfresco ticket used by parallel tests).
    await page.context().clearCookies();
    await page.evaluate(() => sessionStorage.clear());

    await checkA11yWithLogging();
  });

  test("should unsubscribe contributor from interest group", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("Contributor", "password123");

    // Navigate to roles and leave group
    await page.goto("/me/roles");
    await page.locator("[data-cy=leave-group]").first().click();
    await page.locator('[data-cy="ok"]').click();

    // Verify no longer in group
    await page.goto("/me/roles");
    await expect(page.getByText("Test IG title")).not.toBeVisible();

    // Logout
    // Client-only session reset (avoid server-side logout that would expire the
    // shared per-user Alfresco ticket used by parallel tests).
    await page.context().clearCookies();
    await page.evaluate(() => sessionStorage.clear());

    await checkA11yWithLogging();
  });

  test("should verify removed user cannot access document", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("Contributor", "password123");

    // Verify not in group
    await page.goto("/me/roles");
    await expect(page.getByText("Test IG title")).not.toBeVisible();

    // Try to access file details
    if (fileDetailsUrl) {
      await page.goto(fileDetailsUrl);
      await expect(page.getByText(testFileName)).not.toBeVisible();
    }

    await checkA11yWithLogging();
  });
});
