// Test: Create event in agenda
import { test, expect, env } from "./fixtures";

test.describe("Event - Create", () => {
  test("should create event in agenda", async ({
    page,
    login,
    navigateToIGSection,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    await navigateToIGSection("agenda");

    // Click Add event button
    await page.locator('[data-cy="add-event"]').click();

    // Step 1: Basic info
    await page.locator('[data-cy="event-title"]').fill("E2E Test Event");
    await page.locator("#startTime").fill("10:00");
    await page.locator("#endTime").fill("11:00");
    await page.locator('[data-cy="event-location"]').fill("Brussels");

    await page.locator('[data-cy="event-next"]').click();

    // Step 2: Details
    await page.locator("#abstract").fill("Test event for export flow");

    await page.locator('[data-cy="event-next"]').click();

    // Step 3: Audience (open by default)
    await page.locator('[data-cy="event-next"]').click();

    // Step 4: Contact info
    await page.locator('[data-cy="contact-name"]').fill("Test Contact");
    await page.locator('[data-cy="contact-phone"]').fill("+32 123 456");
    await page.locator('[data-cy="contact-email"]').fill("test@europa.eu");

    await page.locator('[data-cy="event-save"]').click();

    // Wait for save
    await page.waitForTimeout(3000);
  });
});
