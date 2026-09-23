// Test: Import Interest Group from a static XML export (uploadedH1.xml)
//
// This test imports a committed fixture file that contains a full CircaBC IG
// export (importRoot schema). It is self-contained — it does not depend on an
// export step — and only requires the target header/category created by tests
// 21 & 22.
import { test, expect, env } from "./fixtures";
import { alfrescoBaseUrl } from "./config";
import * as fs from "fs";
import * as path from "path";

// File-level serial: the migrated-link redirect cases (merged from the former
// 89c spec, below) depend on this import completing AND on the imported nodes
// being Solr-indexed. Under fullyParallel they raced the import across workers
// (resolve endpoint 404 / socket hang up). Serial runs import -> redirects in
// order on one worker.
test.describe.configure({ mode: "serial" });

const IMPORT_FILE = path.join(
  __dirname,
  "..",
  "fixtures",
  "files",
  "uploadedH1.xml",
);

test.describe("IG Import (uploaded file)", () => {
  test("should import interest group from uploadedH1.xml via support page", async ({
    page,
    login,
    checkA11yWithLogging,
  }) => {
    // uploadedH1.xml is a large export; the server-side import can take well
    // over the default 30s per-test budget, so extend it here.
    test.setTimeout(180000);

    // The fixture must exist — it is a committed IG export archive.
    expect(fs.existsSync(IMPORT_FILE)).toBe(true);

    await login(env["admin.username"], env["admin.password"]);

    await page.goto("/support/ig-import");
    await expect(page.locator("h3")).toBeVisible();

    // Step 1: Select target header via mat-select
    const firstSelect = page.locator("mat-select").first();
    await expect(firstSelect).toBeVisible();
    await firstSelect.click();
    await page.getByRole("option", { name: env["header.name"] }).click();

    // Step 2: Select target category via mat-select.
    // The dropdown displays the category's technical name (Category.name,
    // derived from the category admin) rather than its display title, and only
    // one category exists under the test header, so pick the single option.
    const secondSelect = page.locator("mat-select").nth(1);
    await expect(secondSelect).toBeVisible();
    await secondSelect.click();
    await page.getByRole("option").first().click();

    // Step 3: Upload the XML export file (Step 3 only renders once a category
    // is selected).
    const fileInput = page.locator("input[type='file']");
    await expect(fileInput).toBeAttached();
    await fileInput.setInputFiles(IMPORT_FILE);

    // Trigger import
    const importBtn = page.locator("button.import-btn");
    await expect(importBtn).toBeVisible();
    await importBtn.click();

    // Wait for result. uploadedH1.xml is a large export, so allow extra time
    // for the server-side import to complete.
    await expect(page.locator(".result-banner")).toBeVisible({
      timeout: 120000,
    });
    await expect(page.locator(".result-banner")).toContainText("Nodes created");

    // Verify no error banner surfaced
    await expect(page.locator(".error-banner")).not.toBeVisible();

    await checkA11yWithLogging();
  });
});

// ===========================================================================
// Migrated-node link resolution & redirect (merged from former 89c spec).
//
// End-to-end coverage of the "originalNodeRef" feature: OLD deep links whose
// node ids no longer exist on this server are transparently redirected to the
// migrated nodes' NEW urls. Runs AFTER the import above (same file, serial):
// importing uploadedH1.xml re-creates the "HZE Test IG" and stamps its nodes
// with ci:migrated / ci:originalNodeRef (their OLD references). We reuse those
// real, already-stamped references — no privileged setup needed.
// ===========================================================================

/** Matches a /group/<uuid> pair (36-char uuid) anywhere in the path. */
const GROUP_UUID = /\/group\/([0-9a-fA-F-]{36})/;

// --- OLD (pre-migration) ids, verbatim from uploadedH1.xml ------------------
/** interestGroup root (ci:migrated). */
const OLD_IG = "7446c210-fda6-4a78-97fb-8333620e9086";
/** A library space/folder ("MynewTestFolder"). */
const OLD_FOLDER = "30a12b25-9e3e-4642-bdbf-78ae59d64bcc";
/** A library document ("testfile - Copy (103).txt"). */
const OLD_DOC = "5347d3a9-540f-40e7-95b1-c6f289d66ec9";
/** A forum ("Another Forum"). */
const OLD_FORUM = "a7392937-0001-452a-b4a9-e6d2d10d9293";
/** A forum topic ("My new Topic"). */
const OLD_TOPIC = "a1fe0277-a728-4293-a8c6-ca0de6912c7b";

/** Name of the imported IG, rendered as the group <h1>. */
const IG_NAME = "HZE Test IG";

/** All OLD ids whose migrated nodes must be resolvable before the cases run. */
const ALL_OLD_IDS = [OLD_IG, OLD_FOLDER, OLD_DOC, OLD_FORUM, OLD_TOPIC];

/** Fibonacci backoff (ms) between Solr-index polls: 1s, 2s, 3s, 5s, 8s, … */
const POLL_BACKOFF_MS = [1000, 2000, 3000, 5000, 8000, 13000, 21000, 34000];
/** Upper bound on total time spent waiting for the search index to catch up. */
const POLL_MAX_MS = 150000;

/** Obtain a Basic auth header for the Alfresco admin (READ on every node). */
async function adminAuthHeader(
  ctx: import("@playwright/test").APIRequestContext,
): Promise<string> {
  const resp = await ctx.post(
    `${alfrescoBaseUrl}/service/api/login?guest=true`,
    {
      data: {
        username: env["admin.username"],
        password: env["admin.password"],
      },
    },
  );
  const {
    data: { ticket },
  } = await resp.json();
  return "Basic " + Buffer.from(ticket ?? "").toString("base64");
}

/**
 * The backend resolve endpoint is backed by a Solr search on
 * ci:originalNodeRef, so freshly imported nodes only become resolvable once the
 * index catches up. Poll every OLD id until each resolves (HTTP 200), backing
 * off in a Fibonacci sequence, before the redirect cases (which rely on the
 * same search) run.
 */
async function waitForSolrIndexed(
  ctx: import("@playwright/test").APIRequestContext,
  auth: string,
  ids: string[],
): Promise<void> {
  const stillMissing = async (): Promise<string[]> => {
    const checks = await Promise.all(
      ids.map(async (id) => {
        const r = await ctx.get(
          `${alfrescoBaseUrl}/service/circabc/nodes/resolve/${id}`,
          { headers: { Authorization: auth } },
        );
        return { id, ok: r.status() === 200 };
      }),
    );
    return checks.filter((c) => !c.ok).map((c) => c.id);
  };

  let elapsed = 0;
  for (let attempt = 0; ; attempt++) {
    const missing = await stillMissing();
    if (missing.length === 0) return;

    const wait = POLL_BACKOFF_MS[Math.min(attempt, POLL_BACKOFF_MS.length - 1)];
    if (elapsed + wait > POLL_MAX_MS) {
      throw new Error(
        `Solr did not index migrated node(s) [${missing.join(", ")}] ` +
          `within ${POLL_MAX_MS}ms`,
      );
    }
    await new Promise((resolve) => setTimeout(resolve, wait));
    elapsed += wait;
  }
}

/**
 * Navigate to an OLD deep link and assert the guard redirected it to the
 * migrated equivalent: none of the OLD ids remain in the path, we are on the
 * expected route fragment, and we did not fall through to no-content/denied.
 */
async function expectRedirect(
  page: import("@playwright/test").Page,
  oldPath: string,
  mustVanish: string[],
  mustContain: string,
): Promise<void> {
  await page.goto(oldPath);
  await page.waitForURL(
    (url) => {
      const p = url.pathname;
      if (p.includes("/no-content") || p.includes("/denied")) return false;
      if (!p.includes(mustContain)) return false;
      return mustVanish.every((id) => !p.includes(id));
    },
    { timeout: 15000 },
  );
  const path = new URL(page.url()).pathname;
  for (const id of mustVanish) {
    expect(path, `old id ${id} should have been rewritten`).not.toContain(id);
  }
  expect(path).toContain(mustContain);
}

test.describe("Migrated link redirect", () => {
  // Wait until the search index has caught up with the just-completed import,
  // otherwise the resolve endpoint (and hence the guard redirects) return 404.
  test.beforeAll(async ({ playwright }) => {
    test.setTimeout(POLL_MAX_MS + 30000);
    const ctx = await playwright.request.newContext();
    try {
      const auth = await adminAuthHeader(ctx);
      await waitForSolrIndexed(ctx, auth, ALL_OLD_IDS);
    } finally {
      await ctx.dispose();
    }
  });

  // The category admin can access the imported IG (it lives under the test
  // category) and has a known seeded password, so no setup is required.
  test.beforeEach(async ({ login }) => {
    await login(env["category.admin.username"], env["category.admin.password"]);
  });

  test("redirects the interest-group root link", async ({ page }) => {
    await page.goto(`/group/${OLD_IG}`);
    await page.waitForURL(
      (url) => {
        const m = url.pathname.match(GROUP_UUID);
        return !!m && m[1] !== OLD_IG;
      },
      { timeout: 15000 },
    );
    const newId = page.url().match(GROUP_UUID)?.[1];
    expect(newId, "redirect did not land on a /group/<uuid> url").toBeTruthy();
    expect(newId).not.toBe(OLD_IG);
    // Confirm we landed on the migrated interest group.
    await expect(
      page.getByRole("heading", { name: IG_NAME, level: 1 }),
    ).toBeVisible();
  });

  test("redirects a library folder-browse link", async ({ page }) => {
    await expectRedirect(
      page,
      `/group/${OLD_IG}/library/${OLD_FOLDER}`,
      [OLD_IG, OLD_FOLDER],
      "/library/",
    );
  });

  test("redirects a library document-details link", async ({ page }) => {
    await expectRedirect(
      page,
      `/group/${OLD_IG}/library/${OLD_DOC}/details`,
      [OLD_IG, OLD_DOC],
      "/details",
    );
  });

  test("redirects a forum link", async ({ page }) => {
    await expectRedirect(
      page,
      `/group/${OLD_IG}/forum/${OLD_FORUM}`,
      [OLD_IG, OLD_FORUM],
      "/forum/",
    );
  });

  test("redirects a forum topic link", async ({ page }) => {
    await expectRedirect(
      page,
      `/group/${OLD_IG}/forum/topic/${OLD_TOPIC}`,
      [OLD_IG, OLD_TOPIC],
      "/forum/topic/",
    );
  });

  test("redirects an information-service link", async ({ page }) => {
    await expectRedirect(
      page,
      `/group/${OLD_IG}/information`,
      [OLD_IG],
      "/information",
    );
  });

  test("redirects an agenda (events) service link", async ({ page }) => {
    await expectRedirect(page, `/group/${OLD_IG}/agenda`, [OLD_IG], "/agenda");
  });
});
