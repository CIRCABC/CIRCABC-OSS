// Profile lifecycle — create → update → delete.
// Merged from former 83/84/85 specs and run with describe.serial so the
// profile created first is present for update/delete, and the chain retries
// together from a clean state instead of racing across workers.
import { test, expect, env } from "./fixtures";

// File-level serial: the "Profile - Lifecycle" block and the merged
// "Profile - Access Registered" block (was 86) both edit THIS IG's profile
// list. Under fullyParallel they ran as separate files on different workers and
// mutated the profile rows concurrently, so 86's :nth-child(row) selectors hit
// the wrong profile ("cut access" flake). Running the whole file serially keeps
// the profile list stable across both blocks.
test.describe.configure({ mode: "serial" });

test.describe.serial("Profile - Lifecycle", () => {
  test("should create profile", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    await page.goto("/me/roles");
    await page.locator(":nth-child(2) > .group-title").click();
    await page.locator('[data-cy="members"]').waitFor({ state: "visible" });
    await page.locator('[data-cy="members"]').click();
    await page.getByText("Profiles").click();

    // Check if profile already exists (idempotent)
    const profileExists = await page
      .getByText("MyProfile1")
      .first()
      .isVisible()
      .catch(() => false);
    if (profileExists) {
      await checkA11yWithLogging();
      return;
    }

    await page.locator('[data-cy="add-profile"]').click();
    await page.locator('[data-cy="text"]').fill("MyProfile1");
    await page.locator(".modal-footer > .buttons-group > .cta").click();

    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  test("should update profile", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    await page.goto("/me/roles");
    await page.locator(":nth-child(2) > .group-title").click();
    await page.locator('[data-cy="members"]').waitFor({ state: "visible" });
    await page.locator('[data-cy="members"]').click();
    await page.getByText("Profiles").click();

    // Target the profile THIS test created, by text — not by position.
    // Other tests (e.g. 62c forum-moderation) add profiles to the same IG, so a
    // positional :nth-child(6) could land on an unrelated / in-use profile.
    const profileRow = page.locator("tr.row").filter({ hasText: "MyProfile1" });
    await profileRow.first().waitFor({ state: "visible" });
    // Edit is the 2nd span in the row's .actions cell.
    await profileRow
      .first()
      .locator(".cell-name .actions > span:nth-child(2) > a")
      .click();
    // Keep "MyProfile1" as a prefix so the delete step can still find the row.
    await page.locator('[data-cy="text"]').first().fill("MyProfile1 Updated");
    await page.locator(".modal-footer > .buttons-group > .cta").click();

    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });

  test("should delete profile", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login(
      env["interest.group.admin.username"],
      env["interest.group.admin.password"],
    );

    await page.goto("/me/roles");
    await page.getByText(env["interest.group.title"]).first().click();
    // Let the group route settle before touching the tab bar, otherwise the
    // members link re-renders and detaches mid-click ("element not stable").
    await page.waitForURL(/\/group\//, { timeout: 15000 });
    await page.waitForLoadState("networkidle");
    const members = page.locator('[data-cy="members"]');
    await members.waitFor({ state: "visible" });
    await members.click();
    await page.getByText("Profiles").click();

    // Delete the profile THIS test created, targeted by text ("MyProfile1"
    // still matches after the update renamed it to "MyProfile1 Updated").
    // A positional .last() could hit an in-use profile from another test and
    // fail with "Impossible to delete the profile".
    const profileRow = page.locator("tr.row").filter({ hasText: "MyProfile1" });
    await profileRow.first().waitFor({ state: "visible" });
    await profileRow.first().locator('[data-cy="delete-profile"]').click();
    await page.locator('[data-cy="ok"]').click();

    await expect(page.getByText("Success")).toBeVisible();

    await checkA11yWithLogging();
  });
});

// Was 86-profile-access-registered. Kept as its own describe for reporting; the
// file-level serial config above guarantees it runs after the lifecycle block.
test.describe.serial("Profile - Access Registered", () => {
  test("should give access to guest and verify synchronization on registered profile", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("IGadmin1", "password123");

    // Navigate to IG → Members → Profiles
    await page.goto("/me/roles");
    await page.locator(":nth-child(2) > .group-title").click();
    await page.waitForTimeout(500);
    await page.locator('[data-cy="members"]').click();
    await page.getByText("Profiles").click();

    // Edit guest profile
    await page
      .locator(":nth-child(2) > .cell-name > .actions > :nth-child(2) > a")
      .click();

    // Set permissions
    await page
      .locator('input[matSliderThumb][formControlName="information"]')
      .evaluate((el: HTMLInputElement) => {
        el.value = "1";
        el.dispatchEvent(new Event("input"));
        el.dispatchEvent(new Event("change"));
      });
    await page
      .locator('input[matSliderThumb][formControlName="library"]')
      .evaluate((el: HTMLInputElement) => {
        el.value = "1";
        el.dispatchEvent(new Event("input"));
        el.dispatchEvent(new Event("change"));
      });
    await page
      .locator('input[matSliderThumb][formControlName="events"]')
      .evaluate((el: HTMLInputElement) => {
        el.value = "1";
        el.dispatchEvent(new Event("input"));
        el.dispatchEvent(new Event("change"));
      });
    await page
      .locator('input[matSliderThumb][formControlName="newsgroups"]')
      .evaluate((el: HTMLInputElement) => {
        el.value = "1";
        el.dispatchEvent(new Event("input"));
        el.dispatchEvent(new Event("change"));
      });

    await page.locator(".modal-footer > .buttons-group > .cta").click();
    await expect(page.getByText("Success")).toBeVisible();

    // Verify registered profile synchronization
    await page.goto("/me/roles");
    await page.locator(":nth-child(2) > .group-title").click();
    await page.waitForLoadState("networkidle");
    await page.locator('[data-cy="members"]').waitFor({ state: "visible" });
    await page.locator('[data-cy="members"]').click();
    await page.getByText("Profiles").click();
    await page
      .locator(":nth-child(3) > .cell-name > .actions > :nth-child(2) > a")
      .click();

    await expect(
      page.locator('input[matSliderThumb][formControlName="information"]'),
    ).toHaveValue("1");
    await expect(
      page.locator('input[matSliderThumb][formControlName="library"]'),
    ).toHaveValue("1");
    await expect(
      page.locator('input[matSliderThumb][formControlName="events"]'),
    ).toHaveValue("1");
    await expect(
      page.locator('input[matSliderThumb][formControlName="newsgroups"]'),
    ).toHaveValue("1");

    await checkA11yWithLogging();
  });

  test("should cut access to registered and verify synchronization on guest profile", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    await login("IGadmin1", "password123");

    // Navigate to IG → Members → Profiles
    await page.goto("/me/roles");
    await page.locator(":nth-child(2) > .group-title").click();
    await page.waitForTimeout(500);
    await page.locator('[data-cy="members"]').click();
    await page.getByText("Profiles").click();

    // Edit registered profile
    await page
      .locator(":nth-child(3) > .cell-name > .actions > :nth-child(2) > a")
      .click();

    // Revoke permissions
    await page
      .locator('input[matSliderThumb][formControlName="information"]')
      .evaluate((el: HTMLInputElement) => {
        el.value = "0";
        el.dispatchEvent(new Event("input"));
        el.dispatchEvent(new Event("change"));
      });
    await page
      .locator('input[matSliderThumb][formControlName="library"]')
      .evaluate((el: HTMLInputElement) => {
        el.value = "0";
        el.dispatchEvent(new Event("input"));
        el.dispatchEvent(new Event("change"));
      });
    await page
      .locator('input[matSliderThumb][formControlName="events"]')
      .evaluate((el: HTMLInputElement) => {
        el.value = "0";
        el.dispatchEvent(new Event("input"));
        el.dispatchEvent(new Event("change"));
      });
    await page
      .locator('input[matSliderThumb][formControlName="newsgroups"]')
      .evaluate((el: HTMLInputElement) => {
        el.value = "0";
        el.dispatchEvent(new Event("input"));
        el.dispatchEvent(new Event("change"));
      });

    await page.locator(".modal-footer > .buttons-group > .cta").click();
    await expect(page.getByText("Success")).toBeVisible();
    await page.locator('[style="float: right;"] > a').click();

    // Verify guest profile synchronization
    await page.goto("/me/roles");
    await page.locator(":nth-child(2) > .group-title").click();
    await page.waitForLoadState("networkidle");
    await page.locator('[data-cy="members"]').waitFor({ state: "visible" });
    await page.locator('[data-cy="members"]').click();
    await page.getByText("Profiles").click();
    await page
      .locator(":nth-child(2) > .cell-name > .actions > :nth-child(2) > a")
      .click();

    await expect(
      page.locator('input[matSliderThumb][formControlName="information"]'),
    ).toHaveValue("0");
    await expect(
      page.locator('input[matSliderThumb][formControlName="library"]'),
    ).toHaveValue("0");
    await expect(
      page.locator('input[matSliderThumb][formControlName="events"]'),
    ).toHaveValue("0");
    await expect(
      page.locator('input[matSliderThumb][formControlName="newsgroups"]'),
    ).toHaveValue("0");

    await page.locator(".buttons-group > .button").click();

    await checkA11yWithLogging();
  });
});
