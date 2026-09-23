# CircaBC E2E Test Suite - Improvement Recommendations

## Executive Summary

Analysis of the 66 Playwright tests revealed critical dependency issues, execution order problems, and maintainability concerns that can cause cascading test failures.

---

## Critical Issues

### 🔴 Issue 1: Interest Group Deleted Too Early

**Problem:** Test `31-ig-delete-page.spec.ts` deletes the Interest Group, but tests 32-88 require it.

**Impact:** All tests after 31 that reference `env['interest.group.title']` will fail.

**Affected tests:** 32-38 (IG operations), 40-43 (membership), 50-61 (library), 62-75 (forums), 76-77 (topics), 80-88 (admin)

**Fix:** Rename `31-ig-delete-page.spec.ts` → `89-ig-delete-page.spec.ts`

---

### 🔴 Issue 2: Duplicate IG Names

**Problem:** Tests 30 and 32 both create IGs with name "Test IG"

| Test | IG Name |
|------|---------|
| 30-ig-create-page.spec.ts | "Test IG" |
| 32-copy-ig-create-page.spec.ts | "Test IG" (duplicate!) |
| 33-ig-create-second-page.spec.ts | "Test IG 2" |

**Fix:** Update `32-copy-ig-create-page.spec.ts` to use unique name "Copy Test IG"

---

### 🟡 Issue 3: Hardcoded Waits

**Problem:** Tests use `waitForTimeout()` instead of proper assertions.

**Examples:**
```typescript
// 73-help-section-delete-page.spec.ts
await page.waitForTimeout(5000);

// 22-category-create-page.spec.ts  
await page.waitForTimeout(3000);
```

**Fix:** Replace with explicit waits:
```typescript
await page.locator('[data-cy="element"]').waitFor({ state: 'visible' });
```

---

### 🟡 Issue 4: No Idempotency

**Problem:** Tests fail on re-run because entities already exist.

**Example:** `10-user-create-page.spec.ts` fails if users exist.

**Fix:** Add existence checks:
```typescript
const exists = await page.getByText('CircabcAdmin').isVisible().catch(() => false);
if (exists) return;
// proceed with creation
```

---

### 🟡 Issue 5: File State Dependencies

**Problem:** Linear dependency chain with no recovery:
```
52-upload → 54-checkout → 55-checkin → 56a-checkout → 56c-undo → 61-delete
```

**Fix:** Add precondition checks in each test to ensure required state exists.

---

## Recommended Test Order

### Current Order (Problematic)
```
30-ig-create → 31-ig-delete → 32-copy-ig → ... → 88-dynamic-auth
                    ↑
            DELETES IG TOO EARLY
```

### Proposed Order (Fixed)
```
30-ig-create → 32-copy-ig → 33-second-ig → ... → 88-dynamic-auth → 89-ig-delete
                                                                        ↑
                                                              CLEANUP AT END
```

### File Renames Required

| Current | New |
|---------|-----|
| `31-ig-delete-page.spec.ts` | `89-ig-delete-page.spec.ts` |

---

## Implementation Tasks

### Task 1: Fix Test Order (Critical)
- [ ] Rename `31-ig-delete-page.spec.ts` → `89-ig-delete-page.spec.ts`
- [ ] Verify all tests 32-88 pass with IG intact

### Task 2: Fix Duplicate Names (High)
- [ ] Update `32-copy-ig-create-page.spec.ts` to use "Copy Test IG"
- [ ] Update any references to this IG name

### Task 3: Replace Hardcoded Waits (Medium)
Files to update:
- [ ] `22-category-create-page.spec.ts`
- [ ] `35-ig-request-create-page.spec.ts`
- [ ] `52-library-upload-page.spec.ts`
- [ ] `73-help-section-delete-page.spec.ts`
- [ ] `83-profile-create-page.spec.ts`

### Task 4: Add Idempotency (Medium)
- [ ] `10-user-create-page.spec.ts` - check user exists before creating
- [ ] `20-circabc-create-page.spec.ts` - already has check ✓
- [ ] `21-header-create-page.spec.ts` - add header exists check
- [ ] `22-category-create-page.spec.ts` - add category exists check
- [ ] `30-ig-create-page.spec.ts` - add IG exists check

### Task 5: Add Precondition Helpers (Low)
Add to `fixtures.ts`:
```typescript
export async function ensureUserExists(request, username) { }
export async function ensureIGExists(request, igName) { }
export async function ensureFileUploaded(page, request, fileName) { }
```

---

## Test Dependency Map

```
Users (10)
    ↓
CircaBC Admin (20)
    ↓
Header (21)
    ↓
Category (22) ←── Logo tests (23-25)
    ↓
Interest Group (30)
    ├── Copy IG (32)
    ├── Second IG (33)
    ├── FTP Test (34)
    ├── IG Requests (35-38)
    ├── Membership (40-43)
    ├── Library (50-61)
    │       ├── Upload (52)
    │       ├── Check-out (54)
    │       ├── Check-in (55)
    │       └── Delete All (61)
    ├── Forums (62, 75)
    ├── Topics (76-77)
    ├── Profiles (83-86)
    └── Dynamic Auth (87-88)
    ↓
Help System (63-74) ←── Independent of IG
    ↓
System Messages (80-82) ←── Independent of IG
    ↓
IG Cleanup (89) ←── MOVED FROM 31
    ↓
Accessibility (90)
```

---

## Quick Wins

1. **Rename one file** → Fixes critical IG deletion issue
2. **Change one IG name** → Fixes duplicate name conflict
3. **Add 5 existence checks** → Makes tests re-runnable

---

## Long-term Improvements

1. **API-based setup** - Create users/IGs via API instead of UI (10x faster)
2. **Test isolation** - Each test creates/cleans its own data
3. **Parallel execution** - Group independent tests for parallel runs
4. **Retry logic** - Add automatic retry for flaky network operations
