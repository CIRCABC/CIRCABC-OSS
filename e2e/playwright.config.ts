import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: 0,
  workers: 2,
  reporter: [['list']],

  // All specs live in tests/ with numeric prefixes that mirror the Cypress specPattern order.
  // Playwright sorts alphabetically → numbers enforce the correct sequence:
  //   00 seed → 01 home → 02-05 auth → 10 user → 20-25 org
  //   30-38 IGs & requests → 40-43 membership → 50-61 library & docs
  //   62-75 forums & help → 76-77 topics → 80-86 admin → 87-88 dynamic auth → 90 a11y

  use: {
    baseURL: (() => {
      const raw = process.env.BASE_URL || 'http://localhost:4200/ui';
      return new URL(raw).origin;
    })(),
    viewport: { width: 1920, height: 1080 },
    actionTimeout: 15_000,
    ignoreHTTPSErrors: true,
    video: 'off',
    locale: 'en-GB',
    trace: 'on-first-retry',
  },
});
