# CircaBC E2E — Playwright

Complete Playwright test suite migrated from Cypress and extended with full
route/page coverage. **97 test files** covering the application's routes and
features, each running an axe-core accessibility check.

## Setup

```bash
cd e2e
npm install
npx playwright install --with-deps
```

## Prerequisites

- Backend running at `http://localhost:8080` (see `../backend/run.sh build_start`)
- Frontend running at `http://localhost:4200` (see `../frontend/npm start`)

## Running Tests

```bash
npm test                                   # all tests headless (sequential, stops on first failure)
npm run test:headed                        # all tests with browser
npm run test:ui                            # interactive UI mode
npm run test:a11y                          # accessibility audit only
npm run test:after -- <prefix>             # run all tests after the given test
npm run test:list                          # list all tests in order
npm run report                             # open HTML report
```

### Run all tests after a given test

Use `test:after` to resume a test run from a specific point. Pass a numeric
prefix or full filename:

```bash
npm run test:after -- 56b                  # run everything after test 56b
npm run test:after -- 88h                  # run everything after test 88h
npm run test:after -- tests/52-library-upload-page.spec.ts  # full path works too
```

This is useful after fixing a failing test — re-run only the remaining tests
instead of the full suite.

### Run against a remote server

```bash
npm run test:remote                        # all tests against configured remote
npm run test:119                           # remote with max 10 failures
```

Override the built-in remote URL via environment variables:

```bash
BASE_URL=http://localhost/circabc-caas/ui \
ALFRESCO_URL=http://localhost/circabc-caas/alfresco \
npm test
```

### Run a subset by number range

```bash
npx playwright test tests/0*              # auth + personal-area pages (01-08)
npx playwright test tests/5*              # library & document tests (50-61)
npx playwright test tests/6*              # forums & help (62-75)
npx playwright test tests/8*              # profiles, dynamic auth, IG admin (80-89)
npx playwright test tests/9*              # a11y audit, help & error pages (90-92b)
```

### Debug

```bash
npx playwright test --debug               # step-through debugging
npx playwright test --reporter=html       # generate HTML report
```

## Test Execution Order

All spec files live in `tests/` with numeric prefixes that enforce a strict
execution order (Playwright sorts alphabetically → numbers drive the sequence).
Tests build on state created by earlier tests, so ordering is significant:
page-coverage tests are inserted in numbered gaps *after* the tests that create
the data they depend on (e.g. IG page tests after IG creation at 30, IG admin
pages after 88 but before IG deletion at 89).

| Range | Category | Notes |
|-------|----------|-------|
| 01–05 | Basic + auth (home, login, logout, EU SSO) | |
| 06–08 | Personal area pages (dashboard, calendar, account) | render + a11y |
| 10 | User management (create all users via UI) | |
| 20–25 | Organizational (CircaBC, header, category, logos) | |
| 26 | Category detail tabs (details, admins, ig-statistics, support) | after category |
| 29 | Explore → contact category page | render + a11y |
| 30–38 | Interest groups & requests | |
| 31–33 | IG dashboard + agenda (calendar/list) views | after IG (30) |
| 40–43 | Membership | |
| 44, 44a | Members contact + bulk-invite pages | after membership |
| 45–46 | Events & news for export | |
| 50–61 | Library & document workflow | |
| 56b, 56d | Library file details + edit pages | after upload (52) |
| 62–75 | Forums & help system | |
| 76–78 | Topics & forum delete | |
| 79, 79a, 79b | Support pages (user-mgmt, revocation, distribution-list) | CircabcAdmin |
| 80–82 | System messages | |
| 83–86 | Profiles | |
| 87–88 | Dynamic authority | |
| 88a–88j | IG admin pages (general, security, documents, log, logos, keywords, dynamic-properties; auto-upload & external-repository are `test.fixme`) | after 88, before 89 |
| 89, 89b | IG delete & import | cleanup |
| 90 | Accessibility audit | |
| 91–91b | Help pages (about, contact, legal notice) | public |
| 92–92b | Error pages (access denied, no-content, 404) | public |

### Notes on newly added page-coverage tests

- **No hardcoded credentials** — all logins use `env[...]` keys from `config.ts`.
  Tests 06–08 run before user creation (10) so they use the built-in
  `env["admin.*"]`; support pages (79*) use `env["circabc.admin.*"]`.
- **`test.fixme`** marks features that need infra unavailable locally
  (FTP auto-upload `88i`, external repository `88j`, and the pre-existing FTP
  test `34`).
- **a11y everywhere** — every new test calls `checkA11yWithLogging()` (and after
  each view switch) exactly like the migrated tests.
- **No duplicates** — routes already exercised by existing tests are not
  re-tested (e.g. `/explore/group-request` is covered by test 35, category
  `group-requests` by 36/38, category logo/customisation by 23–25, IG admin
  `delete` by 89).

## Files

```
e2e/
├── playwright.config.ts          # config (baseURL, viewport, video)
├── scripts/
│   └── run-tests-after.sh        # helper: run all tests after a given prefix
├── tests/
│   ├── config.ts                 # env variables (mirrors cypress.config.ts)
│   ├── fixtures.ts               # login(), logout(), checkA11yWithLogging()
│   ├── 01-home-page.spec.ts      # first test
│   ├── ...                       # numbered spec files (page + workflow coverage)
│   └── 92b-page-not-found-page.spec.ts  # last test
├── fixtures/files/               # test files (PDF, ZIP, PNG)
└── docs/migration-prd.md         # original test specification
```

## Key Features

- **Sequential dependencies** — tests build on state from previous tests
- **Multi-user workflows** — complex scenarios with different user roles
- **File handling** — PDF, ZIP, PNG uploads via fixtures
- **Cross-origin support** — EU Login/Logout SSO
- **API integration** — efficient login via Alfresco API
- **Accessibility** — built-in a11y checks via axe-core on every test
