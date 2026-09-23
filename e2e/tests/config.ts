/** Environment configuration — mirrors cypress.config.ts env block */
export const env = {
  host: "localhost",
  path: "/alfresco",
  port: 8080,
  circabcAdminCreate: true,
  timeout: 500,
  runA11y: true,
  skipA11yFailures: true,
  logA11yFailures: true,

  "admin.username": "admin",
  "admin.password": "admin",

  "circabc.admin.username": "CircabcAdmin",
  "circabc.admin.password": "password123",

  // Seed-data entity names. Hardcoded to the values the downstream
  // katalon-tests-suite expects (Test Header 1 / Test Category 1 / Test IG title).
  "header.name": "Test Header 1",
  "header.description": "Test Header 1 description",

  "category.name": "Test Category 1",
  "category.description": "Test Category 1 description",
  "category.admin.username": "CatAdmin1",
  "category.admin.password": "password123",
  "category.admin.first.name": "Category1",
  "category.admin.last.name": "Admin",
  "category.admin.email": "Category1.Admin@circabc.eu",
  "category.admin.phone": "123 123",
  "category.admin.postalAddress": "unknown",

  "interest.group.name": "Test IG",
  "interest.group.title": "Test IG title",
  "interest.group.description": "Test IG description",
  "interest.group.contact": "Test IG contact",
  "interest.group.leader": "IGadmin1",
  "interest.group.admin.username": "IGadmin1",
  "interest.group.admin.password": "password123",
  "interest.group.admin.first.name": "Ig1",
  "interest.group.admin.last.name": "Admin",
  "interest.group.admin.email": "Ig1.admin@circabc.eu",
  "interest.group.admin.phone": "123 123",
  "interest.group.admin.postalAddress": "unknown",

  "access.username": "Access",
  "access.password": "password123",
  "access.first.name": "Access",
  "access.last.name": "Access",
  "access.email": "access@circabc.eu",
  "access.phone": "123 123",
  "access.postalAddress": "unknown",

  "author.username": "Author",
  "author.password": "password123",
  "author.first.name": "Author",
  "author.last.name": "Author",
  "author.email": "author@circabc.eu",
  "author.phone": "123 123",
  "author.postalAddress": "unknown",

  "contributor.username": "Contributor",
  "contributor.password": "password123",
  "contributor.first.name": "Contributor",
  "contributor.last.name": "Contributor",
  "contributor.email": "contributor@circabc.eu",
  "contributor.phone": "123 123",
  "contributor.postalAddress": "unknown",

  "reviewer.username": "Reviewer",
  "reviewer.password": "password123",
  "reviewer.first.name": "Reviewer",
  "reviewer.last.name": "Reviewer",
  "reviewer.email": "reviewer@circabc.eu",
  "reviewer.phone": "123 123",
  "reviewer.postalAddress": "unknown",

  "secretary.username": "Secretary",
  "secretary.password": "password123",
  "secretary.first.name": "Secretary",
  "secretary.last.name": "Secretary",
  "secretary.email": "secretary@circabc.eu",
  "secretary.phone": "123 123",
  "secretary.postalAddress": "unknown",

  "invited.user.username": "user1",
  "invited.user.password": "password123",
  "invited.user.first.name": "Invited",
  "invited.user.last.name": "User",
  "invited.user.email": "invited.user@circabc.eu",
  "invited.user.phone": "123 123",
  "invited.user.postalAddress": "unknown",

  "help.section": "Test help section",
} as const;

/** Alfresco base URL derived from env */
export const alfrescoBaseUrl =
  process.env.ALFRESCO_URL || `http://${env.host}:${env.port}${env.path}`;

/**
 * UI base path prefix for page.goto() calls.
 * Locally the Angular dev server serves at root, so no prefix needed.
 * On remote deployments (e.g. /circabc-caas/ui) the prefix is required.
 */
export const basePath = (() => {
  const raw = process.env.BASE_URL || "http://localhost:4200/ui";
  const p = new URL(raw).pathname;
  const normalized = p.endsWith("/") ? p.slice(0, -1) : p;
  return normalized || "";
})();
