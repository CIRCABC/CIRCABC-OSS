import {
  test as base,
  expect,
  type Page,
  type APIRequestContext,
} from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";
import { env, alfrescoBaseUrl, basePath } from "./config";

/**
 * API-based login — mirrors Cypress `cy.login()`.
 */
export async function login(
  page: Page,
  request: APIRequestContext,
  username: string,
  password: string,
) {
  const domain = new URL(process.env.BASE_URL || "http://localhost:4200/ui")
    .hostname;
  await page.context().addCookies([
    {
      name: "cck1",
      value: encodeURIComponent(JSON.stringify({ cm: true, all1st: false })),
      domain,
      path: "/",
    },
  ]);

  const loginResp = await request.post(
    `${alfrescoBaseUrl}/service/api/login?guest=true`,
    { data: { username, password } },
  );
  const {
    data: { ticket },
  } = await loginResp.json();

  const userResp = await request.get(
    `${alfrescoBaseUrl}/service/circabc/users/${username}`,
    {
      headers: {
        Authorization: "Basic " + Buffer.from(ticket).toString("base64"),
      },
    },
  );
  const user = await userResp.json();

  await page.addInitScript(
    ({ ticket, user }) => {
      window.localStorage.setItem("allow-cookies", "yes");
      const ss = window.sessionStorage;
      ss.setItem("ticket", ticket);
      ss.setItem("user.userId", user.userId);
      ss.setItem("user.email", user.email);
      ss.setItem("user.avatar", user.avatar ?? "");
      ss.setItem("user.firstname", user.firstname);
      ss.setItem("user.lastname", user.lastname);
      ss.setItem("user.phone", user.phone ?? "");
      ss.setItem("user.contentFilterLang", user.contentFilterLang ?? "");
      ss.setItem("user.uiLang", user.uiLang ?? "");
      if (user.visibility)
        ss.setItem("user.visibility", user.visibility.toString());
      ss.setItem("user.properties", JSON.stringify(user.properties ?? {}));
    },
    { ticket, user },
  );
}

/**
 * API-based logout — mirrors Cypress `cy.logout()`.
 */
export async function logout(page: Page, request: APIRequestContext) {
  const ticket = await page.evaluate(() =>
    window.sessionStorage.getItem("ticket"),
  );
  if (ticket) {
    await request.delete(
      `${alfrescoBaseUrl}/service/api/login/ticket/${ticket}?format=json`,
      {
        headers: {
          Authorization: "Basic " + Buffer.from(ticket).toString("base64"),
        },
      },
    );
    await page.evaluate(() => {
      const ss = window.sessionStorage;
      for (const key of [
        "ticket",
        "user.userId",
        "user.email",
        "user.avatar",
        "user.first.name",
        "user.last.name",
        "user.phone",
        "user.contentFilterLang",
        "user.uiLang",
        "user.visibility",
        "user.properties",
      ])
        ss.removeItem(key);
    });
  }
}

/**
 * Accessibility check — mirrors Cypress `cy.checkA11yWithLogging()`.
 */
export async function checkA11yWithLogging(page: Page) {
  if (env.timeout > 0) await page.waitForTimeout(env.timeout);
  if (!env.runA11y) return;

  const results = await new AxeBuilder({ page }).analyze();

  if (env.logA11yFailures && results.violations.length > 0) {
    console.error(
      `${results.violations.length} accessibility violation(s) detected.`,
    );
    for (const v of results.violations) {
      console.error(`[${v.impact}] ${v.id}: ${v.description}`);
      for (const n of v.nodes) console.error(`Affected Node: ${n.html}`);
    }
  }

  if (!env.skipA11yFailures) {
    expect(results.violations).toEqual([]);
  }
}

type CircabcFixtures = {
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  checkA11yWithLogging: () => Promise<void>;
  navigateToIGSection: (section: string) => Promise<void>;
};

/**
 * Navigate to an IG section from the /me/roles page.
 * Handles the IG title click → sidebar section click robustly.
 */
export async function navigateToIGSection(page: Page, section: string) {
  await page.goto("/me/roles");
  await page.getByText(env["interest.group.title"]).click();
  const sidebarLink = page.locator(`[data-cy="${section}"]`);
  await sidebarLink.waitFor({ state: "visible" });
  await sidebarLink.scrollIntoViewIfNeeded();
  await sidebarLink.click();
  await page.waitForLoadState("networkidle");
}

/**
 * Precondition helper: Check if user exists via API
 */
export async function userExists(
  request: APIRequestContext,
  username: string,
): Promise<boolean> {
  try {
    const resp = await request.get(
      `${alfrescoBaseUrl}/service/circabc/users/${username}`,
    );
    return resp.ok();
  } catch {
    return false;
  }
}

/**
 * Precondition helper: Check if file exists in library (UI-based)
 */
export async function fileExistsInLibrary(
  page: Page,
  fileName: string,
): Promise<boolean> {
  return page
    .getByText(fileName)
    .first()
    .isVisible()
    .catch(() => false);
}

export const test = base.extend<CircabcFixtures>({
  page: async ({ page }, use) => {
    const originalGoto = page.goto.bind(page);
    page.goto = (url: string, opts?: any) => {
      if (url.startsWith("/")) url = basePath + url;
      return originalGoto(url, opts);
    };
    await use(page);
  },
  login: async ({ page, request }, use) => {
    await use((username, password) => login(page, request, username, password));
  },
  logout: async ({ page, request }, use) => {
    await use(() => logout(page, request));
  },
  checkA11yWithLogging: async ({ page }, use) => {
    await use(() => checkA11yWithLogging(page));
  },
  navigateToIGSection: async ({ page }, use) => {
    await use((section) => navigateToIGSection(page, section));
  },
});

export { expect } from "@playwright/test";
export { env } from "./config";
