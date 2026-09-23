// Test #7: Create CircaBC Instance
import { test, expect, env } from "./fixtures";

test.describe("Create CircaBC Instance", () => {
  test("should create CircaBC instance", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    // The user-directory search used below is Lucene/Solr-backed and therefore
    // eventually consistent, so it may need to be retried while the user that
    // test #10 just created gets indexed. Give the test headroom over the 30s
    // default; the happy path still finishes in a couple of seconds.
    test.setTimeout(90_000);

    await login(env["admin.username"], env["admin.password"]);

    // Navigate to CircaBC admin page
    await page.goto("/admin/circabc");

    // The administrator list loads asynchronously. Wait until it has settled
    // (either an existing row or the "none" message is shown) before deciding
    // whether CircabcAdmin already has the role — checking too early races the
    // list load and would wrongly re-invite an existing administrator.
    const existingAdmin = page.getByText("CircabcAdmin");
    const noAdmins = page.getByText("No CIRCABC administrators to display");
    await expect(existingAdmin.or(noAdmins).first()).toBeVisible({
      timeout: 15_000,
    });

    if (await existingAdmin.first().isVisible()) {
      // Admin already exists, test passes
      await checkA11yWithLogging();
      return;
    }

    // Invite CircabcAdmin as CircaBC administrator
    await page.getByText("Invite CIRCABC administrator").click();

    // Search for CircabcAdmin and pick it from the results.
    //
    // The modal only queries the backend when the Search button is clicked, and
    // that search is Lucene/Solr-backed (eventually consistent): the user
    // created moments earlier in test #10 may not be indexed yet, so the first
    // search can return zero results. Passively waiting on the (empty) result
    // list never resolves — instead re-run the search until the freshly created
    // admin is indexed and appears.
    const searchBox = page.getByRole("textbox", { name: "Search" });
    const searchButton = page.getByRole("button", { name: "Search" });
    const adminOption = page.getByRole("option", {
      name: "Circabc Admin (admin@circabc.eu)",
    });

    await expect(async () => {
      await searchBox.fill("CircabcAdmin");
      await searchButton.click();
      await expect(adminOption).toBeVisible({ timeout: 5_000 });
    }).toPass({ timeout: 45_000, intervals: [1_000, 2_000, 3_000, 5_000] });

    await adminOption.click();

    // Add selection to invitations
    await page.getByRole("button", { name: "Add selection" }).click();

    // Create the invitation
    await page.getByText("Create").click();

    // Verify admin appears in the table (backend round-trip + list refresh)
    await expect(page.getByText("CircabcAdmin").first()).toBeVisible({
      timeout: 15_000,
    });

    await checkA11yWithLogging();
  });
});
