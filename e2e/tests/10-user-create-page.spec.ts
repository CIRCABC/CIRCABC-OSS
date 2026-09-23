// spec: docs/migration-prd.md
// Test #6: Create Users - creates all required users via admin UI

import { test, expect, env } from "./fixtures";

test.describe("Create Users", () => {
  test.beforeEach(async ({ login }) => {
    await login(env["admin.username"], env["admin.password"]);
  });

  const users = [
    {
      username: "CircabcAdmin",
      firstName: "Circabc",
      lastName: "Admin",
      email: "admin@circabc.eu",
    },
    {
      username: "CatAdmin1",
      firstName: "Category1",
      lastName: "Admin",
      email: "Category1.Admin@circabc.eu",
    },
    {
      username: "IGadmin1",
      firstName: "Ig1",
      lastName: "Admin",
      email: "Ig1.admin@circabc.eu",
    },
    {
      username: "Access",
      firstName: "Access",
      lastName: "Access",
      email: "access@circabc.eu",
    },
    {
      username: "Author",
      firstName: "Author",
      lastName: "Author",
      email: "author@circabc.eu",
    },
    {
      username: "Contributor",
      firstName: "Contributor",
      lastName: "Contributor",
      email: "contributor@circabc.eu",
    },
    {
      username: "Reviewer",
      firstName: "Reviewer",
      lastName: "Reviewer",
      email: "reviewer@circabc.eu",
    },
    {
      username: "Secretary",
      firstName: "Secretary",
      lastName: "Secretary",
      email: "secretary@circabc.eu",
    },
    {
      username: "user1",
      firstName: "Invited",
      lastName: "User",
      email: "invited.user@circabc.eu",
    },
  ];

  for (const user of users) {
    test(`should create user ${user.username}`, async ({
      page,
      checkA11yWithLogging,
    }) => {
      await page.goto("/me/roles");

      await page.locator('[data-cy="create-user"]').click();
      await page.locator('[data-cy="username"]').fill(user.username);
      await page.locator('[data-cy="firstname"]').fill(user.firstName);
      await page.locator('[data-cy="lastname"]').fill(user.lastName);
      await page.locator('[data-cy="email"]').fill(user.email);
      await page.locator('[data-cy="phone"]').fill("123 123");
      await page.locator('[data-cy="postalAddress"]').fill("unknown");
      await page.locator('[data-cy="password"]').fill("password123");
      await page.locator('[data-cy="passwordVerify"]').fill("password123");
      await page.locator('[data-cy="create"]').click();

      await expect(page.getByText("Success")).toBeVisible();

      await checkA11yWithLogging();
    });
  }
});
