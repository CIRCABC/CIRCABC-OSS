// Test #40: Forum Moderation - Comment Visibility
import { test, expect, env } from "./fixtures";

test.describe("Forum Moderation - Comment Visibility", () => {
  const forumName = "ModeratedForum";
  const topicName = "ModeratedTopic";
  const commentText = "Test comment for moderation";
  const profileName = "ModeratorProfile" + Date.now();

  test("should display pending comments to member with moderate rights", async ({
    page,
    login,
    checkA11yWithLogging,
    navigateToIGSection,
  }) => {
    test.setTimeout(120000);
    // Step 1: Login as IG admin
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    // Step 2: Create profile with moderate rights on forum and invite member
    await navigateToIGSection("members");
    await page.getByText("Profiles").click();
    await page.waitForTimeout(1000);

    // Create profile with moderate rights on forum
    await page.getByText("Add profile").click();
    await page.locator('[data-cy="text"]').first().fill(profileName);
    // Set newsgroups slider to moderate (value 3 = NwsModerate)
    await page.locator("#newsgroups").evaluate((el: HTMLInputElement) => {
      el.value = "3";
      el.dispatchEvent(new Event("input"));
    });
    await page.locator(".modal-footer .cta").first().click();
    await expect(page.getByText("Success")).toBeVisible();
    await page.getByText("Close").first().click();

    // Invite access user with ModeratorProfile
    await page
      .locator("cbc-reponsive-sub-menu")
      .getByRole("link", { name: "Members" })
      .click();
    await page.waitForTimeout(500);
    await page.locator('[data-cy="actions"]').click();
    await page.locator('[data-cy="invite-member"]').click();
    await page
      .locator('[data-cy="search-user"]')
      .fill(env["contributor.username"]);
    await page.locator('[data-cy="search-button"]').click();
    await page.waitForTimeout(1000);
    const option = await page
      .locator("[data-cy=users] option:not(:disabled)")
      .first()
      .textContent();
    await page
      .locator("[data-cy=users]")
      .selectOption({ label: option!.trim() });
    await page.locator("[data-cy=select]").click();
    await page.waitForTimeout(500);

    await page
      .locator("#profileName")
      .selectOption(profileName, { force: true });
    await page.locator("[data-cy=ok]").click({ force: true });
    await expect(page.getByText("Success")).toBeVisible();

    // Step 3: Create forum and topic
    await navigateToIGSection("forums");
    await page.locator("[data-cy=add]").click();
    await page.locator("[data-cy=create-forum]").click();
    await page.locator('[data-cy="text"]').fill(forumName);
    await page
      .locator('[data-cy="textarea"] .ql-editor')
      .fill("Forum with moderation");
    await page.locator('[data-cy="ok"]').click();

    // Navigate back to forums and click on the new forum
    await navigateToIGSection("forums");
    await page.getByText(forumName).click();
    await page.locator("[data-cy=add]").click();
    await page.locator("[data-cy=create-topic]").click();
    await page.locator('[data-cy="text"]').fill(topicName);
    await page
      .locator('[data-cy="textarea"] .ql-editor')
      .fill("Topic for moderation test");
    await page.locator('[data-cy="ok"]').click();

    // Step 4: Enable moderation on forum
    await navigateToIGSection("forums");
    await page.getByText(forumName).click();
    await page.getByText("Moderation").click();
    await page
      .locator("#moderation")
      .evaluate((el: HTMLInputElement) => el.click());
    await page.getByText("Accept", { exact: true }).click();

    // Step 5: Add comment to topic
    await page.getByText(topicName).click();
    await page.getByText("Add a new comment").click();
    await page.locator(".ql-editor").fill(commentText);
    await page.getByText("Post", { exact: true }).click();
    await page.waitForTimeout(2000);

    // Step 6: Verify approve/reject links are visible for admin
    await expect(page.getByText("Approve").first()).toBeVisible();
    await expect(page.getByText("Reject").first()).toBeVisible();

    // Step 7: Logout and login as member with moderate rights
    await page.context().clearCookies();
    await login(env["contributor.username"], env["contributor.password"]);

    // Step 8: Navigate to forum/topic and verify comment is visible
    await navigateToIGSection("forums");
    await page.getByText(forumName).click();
    await page.getByText(topicName).click();

    // Expected: Comment should be visible to member with moderate rights
    await expect(page.getByText(commentText)).toBeVisible();

    await checkA11yWithLogging();
  });
});
