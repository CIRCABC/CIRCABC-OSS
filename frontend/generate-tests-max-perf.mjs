#!/usr/bin/env npx zx

import { existsSync } from "fs";
import { globby } from "zx";

const parallel = argv.parallel ?? argv.p ?? 1;
const target = argv._[0];

let files;
if (target) {
  files = [target];
} else {
  const allFiles = await globby(
    [
      "src/**/*.ts",
      "!src/**/*.spec.ts",
      "!src/**/*.d.ts",
      "!src/**/generated/**",
      "!src/environments/**",
      "!src/polyfills.ts",
      "!src/main.ts",
      "!src/**/index.ts",
      // Simple type/interface/enum/constant files — no logic to test
      "!src/app/app-info.ts",
      "!src/circabc-preset.ts",
      "!src/app/core/variables.ts",
      "!src/app/core/ui-model/**",
      "!src/app/core/message/ui-message.ts",
      "!src/app/core/message/ui-message-level.ts",
      "!src/app/core/evaluator/permissions.ts",
      "!src/app/action-result/action-result.ts",
      "!src/app/action-result/action-type.ts",
      "!src/app/help/contact-support/reasons-enum.ts",
      "!src/app/group/dynamic-properties/title/title.ts",
      "!src/app/group/dynamic-properties/type/dynamic-property-type.ts",
      "!src/app/group/dynamic-properties/type/dynamic-property-types.ts",
      "!src/app/group/listing-options/listing-options.ts",
      "!src/app/group/library/encodings/encodings.ts",
      "!src/app/group/library/mimetypes/supported-mimetypes.ts",
      "!src/app/group/library/upload-form/file-upload-item.ts",
      "!src/app/group/agenda/timezones/supported-timezones.ts",
      "!src/app/group/admin/summary/structure-tree/structure-node.ts",
      "!src/app/group/members/invite-user/restorable-user-profile.ts",
      "!src/app/group/permissions/add/permission-definition-model.ts",
      "!src/app/shared/langs/supported-langs.ts",
      "!src/app/shared/treeview/tree-node.ts",
      "!src/app/shared/file-extension-icon/known-extensions.ts",
      "!src/app/shared/add-notifications/notification-definition-model.ts",
      "!src/app/shared/pipes/timezonehelper.ts",
      "!src/app/support/user-management/interest-group-profile-selectable.ts",
      "!src/app/support/user-management/users-memberships-model.ts",
      "!src/app/support/distribution-list/selectable-paged-distribution-mails.ts",
      // Routing modules — just route config arrays, no logic
      "!src/**/*-routing.module.ts",
      "!src/app/app-routes.ts",
      // Transloco config module
      "!src/app/transloco/**",
    ],
    { cwd: process.cwd() }
  );
  files = allFiles.filter(
    (f) => !existsSync(f.replace(/\.ts$/, ".spec.ts"))
  );
}

console.log(`Generating tests for ${files.length} file(s) with ${parallel} worker(s)`);

const prompt = (file) =>
  `Write a unit test for ${file}. Create the .spec.ts file next to the source file.

CRITICAL — This project uses Angular 21 with Vitest (NOT Jasmine/Karma). TypeScript strict mode is enabled.

## Project Setup
- tsconfig baseUrl is "src", so imports use paths like 'app/core/...' (not relative '../' or '@app/')
- tsconfig.spec.json has "types": ["vitest/globals"] — describe, it, expect, beforeEach, afterEach are available globally without importing
- Only import { vi } from 'vitest' when you need vi.fn() or vi.spyOn()
- Components are standalone (Angular 21 default) and use inject() for DI, not constructor injection

## Test Framework: Vitest
- Use vi.fn() to create mock functions
- Use vi.spyOn(object, 'method') for spying on existing objects
- For mock services, create plain objects with vi.fn() methods:
    const mockService = { myMethod: vi.fn().mockReturnValue(of(result)) }
- Provide mocks via TestBed: { provide: RealService, useValue: mockService }
- NEVER use: jasmine, jasmine.createSpyObj, global spyOn, spyOnProperty, fail, expectAsync
- NEVER import from '@jsverse/transloco/testing' — that module does not exist

## Vitest Matchers (IMPORTANT)
- .toBe(true) / .toBe(false) — NEVER use .toBeTrue() / .toBeFalse() (they don't exist in Vitest)
- .toBeTruthy() / .toBeFalsy() for loose boolean checks
- .toEqual() for deep object equality
- .toHaveBeenCalled(), .toHaveBeenCalledWith(...)
- .toBeDefined(), .toBeNull(), .toBeUndefined()
- .toThrow(), .rejects.toThrow() for error cases

## Angular Testing Patterns
- Use TestBed.configureTestingModule({ providers: [...] }) for services
- Use TestBed.configureTestingModule({ imports: [ComponentUnderTest], providers: [...] }) for components
- Use provideHttpClient() + provideHttpClientTesting() for HTTP services (import from @angular/common/http and @angular/common/http/testing)
- For components/services using Transloco, add to providers:
    provideTransloco({ config: { defaultLang: 'en', availableLangs: ['en'] } })
  and provide a mock loader: { provide: TranslocoLoader, useValue: { getTranslation: vi.fn().mockReturnValue(of({})) } }
- Use fixture.detectChanges() after creating component fixtures
- Use TestBed.inject(ServiceClass) to get service instances

## Type Safety (strict mode)
- All mock data must satisfy TypeScript strict checks — no implicit any
- For generated API models (from src/app/core/generated/), read the interface to know required fields, then provide ALL required fields in mocks
- Use type assertions only when necessary: { ...minimalData } as SomeType
- Typed mock functions: vi.fn<[], ReturnType>() or vi.fn().mockReturnValue(typedValue)
- Parameters in callbacks must be typed explicitly

## General
- Test both happy path and error cases
- Keep tests isolated and independent
- Use descriptive test names: describe('ServiceName', () => { it('should do X when Y', ...) })
- Do NOT import anything from jasmine, karma, or @types/jasmine

Only create the spec file, do not modify the source.`;

const MAX_FIX_ATTEMPTS = 3;

async function processFile(file) {
  const specFile = file.replace(/\.ts$/, ".spec.ts");
  console.log(`\n--- Generating test for: ${file} ---`);

  try {
    await $`kiro-cli chat --model claude-opus-4.6 --no-interactive --trust-all-tools ${prompt(file)}`;
  } catch (e) {
    console.error(`Failed to generate for ${file}: ${e.message}`);
    return;
  }

  for (let attempt = 1; attempt <= MAX_FIX_ATTEMPTS; attempt++) {
    try {
      const result =
        await $`npx ng test --no-watch --include=${specFile} 2>&1`.nothrow();
      if (result.exitCode === 0) {
        console.log(`✅ ${specFile} passes`);
        break;
      }

      if (attempt === MAX_FIX_ATTEMPTS) {
        console.error(
          `❌ ${specFile} still failing after ${MAX_FIX_ATTEMPTS} fix attempts, deleting`
        );
        await $`rm -f ${specFile}`;
        break;
      }

      console.log(
        `⚠️  ${specFile} failed (attempt ${attempt}/${MAX_FIX_ATTEMPTS}), fixing...`
      );
      const errors = result.stdout.slice(-3000);
      await $`kiro-cli chat --model claude-opus-4.6 --no-interactive --trust-all-tools ${`Fix the failing test in ${specFile}. Here are the errors:\n\n${errors}\n\nFix the spec file so it compiles and passes. Do not modify the source file. Remember: this project uses Vitest (not Jasmine), vitest/globals are available globally, use vi.fn() for mocks, use TRANSLOCO_LOADER (not TranslocoLoader) as provider token, and all generated model mocks must include ALL required fields.`}`;
    } catch (e) {
      console.error(`Error running test for ${specFile}: ${e.message}`);
      break;
    }
  }
}

// Worker pool — each worker grabs the next file as soon as it finishes
let idx = 0;
const next = () => (idx < files.length ? files[idx++] : null);

const worker = async () => {
  let file;
  while ((file = next())) {
    await processFile(file);
  }
};

await Promise.all(Array.from({ length: parallel }, () => worker()));
