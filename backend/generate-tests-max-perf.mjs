#!/usr/bin/env npx zx

import { existsSync } from "fs";
import { basename } from "path";
import { globby } from "zx";

const parallel = argv.parallel ?? argv.p ?? 1;
const target = argv._[0];

let files;
if (target) {
  files = [target];
} else {
  const allFiles = await globby(
    [
      "src/main/java/**/*.java",
      // Exclude model/DTO classes — just getters/setters, no logic
      "!src/main/java/io/swagger/model/**",
      // Exclude Alfresco model constants
      "!src/main/java/io/swagger/model/alfresco/**",
      // Exclude exception classes — trivial
      "!src/main/java/io/swagger/exception/**",
      // Exclude config classes
      "!src/main/java/io/swagger/config/**",
      // Exclude interfaces (Api contracts with no implementation)
      "!src/main/java/io/swagger/api/*Api.java",
      // Exclude simple enums/constants
      "!src/main/java/io/swagger/api/ClipboardAction.java",
      "!src/main/java/io/swagger/api/PostNode.java",
      "!src/main/java/io/swagger/api/FileAttachmentData.java",
    ],
    { cwd: process.cwd() }
  );

  files = allFiles.filter((f) => {
    const testPath = f
      .replace("src/main/java/", "src/test/java/")
      .replace(/\.java$/, "Test.java");
    return !existsSync(testPath);
  });
}

console.log(`Generating tests for ${files.length} file(s) with ${parallel} worker(s)`);

const prompt = (file) =>
  `Write a unit test for ${file}. Create the test file in the mirrored path under src/test/java/ with the suffix Test.java.

CRITICAL — This project uses JUnit 4 + Mockito 4.2. Java 21. Maven build.

## Project Setup
- Source: src/main/java/
- Tests: src/test/java/ (mirror the package structure)
- Test class name: <ClassName>Test.java
- Dependencies: junit 4.13.1, mockito-core 4.2.0, alfresco-remote-api (provided)

## Test Framework Rules
- Use @Test from org.junit.Test, @Before from org.junit.Before
- Use static imports: org.junit.Assert.*, org.mockito.Mockito.*
- Use Mockito.mock() directly — do NOT use @Mock, @InjectMocks, @RunWith, MockitoAnnotations
- Do NOT use JUnit 5 (@BeforeEach, @ExtendWith, Assertions.*)
- Do NOT use Spring test context (@SpringBootTest, @Autowired in tests)
- Do NOT use mockito-inline or static mocking (mockStatic) — it's not available

## Dependency Injection Pattern (IMPORTANT)
Classes use @Autowired private fields. Inject mocks via reflection:
\`\`\`java
private void setField(String fieldName, Object value) throws Exception {
    Field field = ClassUnderTest.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(instanceUnderTest, value);
}
\`\`\`
Check the source file for exact field names — they must match exactly.

## AuthenticationUtil (CRITICAL)
If the source uses AuthenticationUtil (getRunAsUser, setRunAsUserSystem, etc.), you MUST initialize it in @Before:
\`\`\`java
Field initialized = AuthenticationUtil.class.getDeclaredField("initialized");
initialized.setAccessible(true);
initialized.set(null, true);
Field guest = AuthenticationUtil.class.getDeclaredField("defaultGuestUserName");
guest.setAccessible(true);
guest.set(null, "guest");
AuthenticationUtil.setFullyAuthenticatedUser("testuser");
\`\`\`

## Alfresco Mocking
- NodeRef: new NodeRef("workspace://SpacesStore/test-id") or new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id")
- Mock all Alfresco services: NodeService, SearchService, PersonService, AuthorityService, FileFolderService, etc.
- For SearchService.query(): mock ResultSet, ResultSetRow, and Iterator<ResultSetRow>
- For overloaded methods: use explicit casts to disambiguate null arguments, e.g. (Date) null

## Test Design
- Test happy path + error/edge cases (null inputs, empty collections, exceptions)
- Use @Test(expected = SomeException.class) for expected exceptions
- Keep tests isolated — mock everything, no shared mutable state
- Method names: testMethodName_whenCondition_thenExpectedResult
- For large classes, focus on the most important 3-5 public methods

Only create the test file, do not modify the source.`;

const MAX_FIX_ATTEMPTS = 5;

async function processFile(file) {
  const testFile = file
    .replace("src/main/java/", "src/test/java/")
    .replace(/\.java$/, "Test.java");
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
        await $`mvn test -pl . -Dtest=${basename(testFile, ".java")} -DfailIfNoTests=false 2>&1`.nothrow();
      if (result.exitCode === 0) {
        console.log(`✅ ${testFile} passes`);
        break;
      }

      if (attempt === MAX_FIX_ATTEMPTS) {
        console.error(
          `❌ ${testFile} still failing after ${MAX_FIX_ATTEMPTS} fix attempts, deleting`
        );
        await $`rm -f ${testFile}`;
        break;
      }

      console.log(
        `⚠️  ${testFile} failed (attempt ${attempt}/${MAX_FIX_ATTEMPTS}), fixing...`
      );
      const errors = result.stdout.slice(-3000);
      await $`kiro-cli chat --model claude-opus-4.6 --no-interactive --trust-all-tools ${`Fix the failing test in ${testFile}. Here are the errors:\n\n${errors}\n\nFix the test file so it compiles and passes. Do not modify the source file.

Rules:
- JUnit 4 only: org.junit.Test, org.junit.Before, org.junit.Assert.*
- Mockito 4.2 only: Mockito.mock(), Mockito.when(), Mockito.verify() — NO @Mock, @InjectMocks, @RunWith
- NO JUnit 5, NO Spring test context, NO mockito-inline/mockStatic
- Inject mocks via reflection (Field.setAccessible + Field.set) — field names must match source exactly
- If AuthenticationUtil error: initialize via reflection (set "initialized" to true, "defaultGuestUserName" to "guest") then call setFullyAuthenticatedUser
- For ambiguous overloaded methods: cast null args explicitly, e.g. (Date) null
- For missing method errors: read the source file to find the correct method signature`}`;
    } catch (e) {
      console.error(`Error running test for ${testFile}: ${e.message}`);
      break;
    }
  }
}

// Worker pool
let idx = 0;
const next = () => (idx < files.length ? files[idx++] : null);

const worker = async () => {
  let file;
  while ((file = next())) {
    await processFile(file);
  }
};

await Promise.all(Array.from({ length: parallel }, () => worker()));
