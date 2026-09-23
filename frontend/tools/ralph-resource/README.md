# Ralph loop — `async ngOnInit` → `resource()` migration

An autonomous [Ralph loop](https://ghuntley.com/ralph/) (after Geoffrey Huntley)
implemented with [google/zx](https://github.com/google/zx). It grinds through
the frontend one component at a time, using `kiro-cli` headless to convert
imperative `async ngOnInit()` data loading into reactive Angular
[`resource()`](https://angular.dev/guide/signals/resource) + signals, then
verifies and (optionally) commits each change.

It is a sibling of [`../ralph`](../ralph) (the OnPush migration) and reuses the
same harness structure, gates, and safety model.

## Why — the migration this drives

The user's directive: **use `resource()` and signals; do not use
`async ngOnInit`** for fetching data. Many components still load data
imperatively:

```ts
async ngOnInit() {
  this.loading.set(true);
  try { this.foo.set(await this.fooService.getFooAsync({ id })); }
  catch { this.restCallError.set(true); }
  this.loading.set(false);
}
```

The reactive equivalent is declarative, re-runs when its inputs change, and
gives `isLoading`/`status()`/`error()` for free:

```ts
private readonly fooResource = resource({
  params: () => this.id() || undefined,        // idle while empty
  loader: ({ params: id }) => this.fooService.getFooAsync({ id }),
});
public readonly foo = computed(() =>
  this.fooResource.hasValue() ? this.fooResource.value() : []
);
public readonly loading = this.fooResource.isLoading;
public readonly restCallError = computed(() => this.fooResource.status() === 'error');
```

The full per-component playbook — including the important gotcha that
`resource.value()` **throws** in the error state (guard with `hasValue()` or
catch in the loader), the idle-when-`undefined` `params` pattern, pagination via
a `page` signal, and how to update specs to drive resources with
`fixture.detectChanges()` + `await fixture.whenStable()` — lives in
[`improve-prompt.md`](./improve-prompt.md), which is the exact prompt the agent
receives each iteration.

> Note: the generated API clients also expose `...Resource` (rxResource)
> helpers, but per the user's preference this migration wraps the promise-based
> `...Async` methods in `resource()` instead.

## How it works

Each iteration:

1. Pick the next `pending` `*.component.ts` from the persisted queue
   (`.state/progress.json`). The queue contains **only** components that still
   contain an `async ngOnInit`, discovered from disk (specs and
   `core/generated/**` excluded).
2. Run `kiro-cli chat --no-interactive --trust-all-tools` with
   [`improve-prompt.md`](./improve-prompt.md), scoped to that one component.
3. Guard: if `async ngOnInit` is still present after the agent runs (or the
   agent reports `RALPH_BLOCKED`), the change is reverted / marked
   `blocked` — the tree never drifts from the queue.
4. Run verification gates in order: `npm run tsc-go` (fast whole-project
   type-check via tsgo), `npm run lint-biome` (Biome on `./src`), the changed
   component's `.spec.ts` via `npm run test:file`, and finally `npm run build`
   (`ng build`).
5. The `npm run build` gate runs for **every** component — it is the only gate
   that runs Angular's whole-app AOT template type-check, so it catches
   template↔TS signal-call mismatches (`{{ foo }}` vs `{{ foo() }}`) and errored
   `resource.value()` reads that `tsc-go`, Biome, and a shallow spec can miss. If
   the build fails, the agent gets up to `--build-fix-attempts` scoped fixes
   (which become part of the same component's commit); if it still fails, that
   one component is reverted and marked `failed`.
6. On success: mark `done` and optionally `git commit`. On any gate failure:
   `git checkout --` the component's files (revert) and mark `failed`.
7. Persist progress after every component, so the run is fully resumable and
   safe to Ctrl-C.
8. A redundant final `npm run build` runs at the end of the run as a safety net
   (disable with `--no-final-build`). Because each component is already
   build-verified, the batch `--build-every` checkpoint is **off by default**.

## Requirements

- `kiro-cli` on `PATH` (headless mode)
- Node + the repo's dev deps installed (`zx` is already in `devDependencies`)
- Run from the `frontend/` directory

## Usage

Run everything from the `frontend/` directory. Each invocation **is** the loop:
it walks the queue one component at a time until it reaches `--limit`, runs out
of pending components, or aborts after `--max-failures` consecutive failures.

```bash
# (recommended) work on a dedicated branch so a batch is easy to review or drop
git switch -c chore/ralph-resource

# See the queue and what would run — no agent calls, no file changes
npx zx tools/ralph-resource/ralph.mjs --dry-run

# (optional) print the exact prompt the agent will get for the next component
npx zx tools/ralph-resource/ralph.mjs --dry-run --print-prompt

# Do ONE component end-to-end, without committing (recommended first real run)
npx zx tools/ralph-resource/ralph.mjs --limit=1

# A reviewable batch, committing each success (never pushes)
npx zx tools/ralph-resource/ralph.mjs --limit=10 --commit

# Process every pending component, committing each success. Long-running.
npx zx tools/ralph-resource/ralph.mjs --all --commit
```

The run is **fully resumable**: progress is saved after every component in
`.state/progress.json`. If it aborts or you Ctrl-C, fix the cause and re-run the
**same command** — it continues the backlog and skips anything already `done`.

```bash
# Keep re-running until the backlog is empty, pausing on repeated failures
until npx zx tools/ralph-resource/ralph.mjs --all --commit; do
  echo "Ralph aborted — inspect .state/report.md, fix the cause, then press Enter to resume"
  read _
done

# Scope to a subtree, check progress, or reset the queue
npx zx tools/ralph-resource/ralph.mjs --only=help --commit
npx zx tools/ralph-resource/ralph.mjs --status
npx zx tools/ralph-resource/ralph.mjs --rebuild-queue --dry-run
```

### Options

| Option                   | Default             | Meaning                                                    |
| ------------------------ | ------------------- | ---------------------------------------------------------- |
| `--limit=N`              | `1`                 | Max components this run                                    |
| `--all`                  | —                   | Process every pending component                            |
| `--only=<substr>`        | —                   | Restrict to paths containing `<substr>`                    |
| `--commit`               | off                 | Commit each success (respects git hooks; **never pushes**) |
| `--dry-run`              | —                   | Build/show the queue and plan only; no agent calls         |
| `--print-prompt`         | —                   | With `--dry-run`, print the rendered prompt for the next   |
| `--status`               | —                   | Print progress summary and exit                            |
| `--rebuild-queue`        | —                   | Recompute the queue from disk (discards saved statuses)    |
| `--skip-lint`            | off                 | Skip the `npm run lint-biome` gate                         |
| `--skip-test`            | off                 | Skip the changed component's spec gate                     |
| `--skip-build`           | off                 | Skip the per-component `npm run build` gate (**not recommended**) |
| `--build-every=N`        | `0` (off)           | Also run a batch `npm run build` every N migrations (redundant)    |
| `--no-final-build`       | —                   | Skip the extra `npm run build` at the end of the run       |
| `--build-fix-attempts=N` | `2`                 | Agent attempts to fix a failing build before reverting     |
| `--build-timeout=<d>`    | `20m`               | Timeout for each `npm run build` (zx duration)             |
| `--max-failures=N`       | `3`                 | Abort after N consecutive failures                         |
| `--model=<id>`           | `claude-opus-4.8`   | Model passed to `kiro-cli`                                 |
| `--effort=<level>`       | `high`              | Reasoning effort: `low\|medium\|high\|xhigh\|max`          |
| `--skill=<name>`         | `angular-developer` | Skill loaded via the prompt (`--no-skill` to disable)      |
| `--agent=<name>`         | default agent       | `kiro-cli` agent to use                                    |
| `--agent-timeout=<d>`    | `20m`               | Per-component agent timeout (zx duration)                  |
| `-h`, `--help`           | —                   | Show help                                                  |

Each iteration invokes:

```bash
kiro-cli chat --no-interactive --trust-all-tools \
  --model claude-opus-4.8 --effort high "<scoped prompt>"
```

## Safety

- **No pushes**, ever. Commits are **opt-in** via `--commit`.
- One component per iteration → small, reviewable diffs.
- Every change passes `tsc-go` + `lint-biome` + the component spec + a full
  `npm run build` before it is kept; failures are auto-reverted so the tree
  never holds a broken component and the build stays green after every commit.
- An extra guard reverts the change if `async ngOnInit` is somehow still present
  after the agent claims success.
- Components whose `ngOnInit` only does side effects (not data loading) are left
  untouched and marked `blocked` — `resource()` is the wrong tool for those.
- The per-component `npm run build` gate adds whole-app AOT template
  type-checking on top of the fast per-component gates, so a template↔signal
  mismatch is caught immediately for the component that introduced it.
- Only the target component's own files are edited (enforced by the prompt).
- Fully resumable: state lives in `.state/progress.json`; re-running continues
  where it left off. `--status` shows the summary; `.state/report.md` lists
  anything `failed`/`blocked` for manual attention.

## State & output (git-ignored)

```
tools/ralph-resource/.state/
  progress.json     # the resumable queue with per-component status
  report.md         # summary + items needing attention
  logs/*.log        # full agent transcript per component
  logs/build-*.log  # full 'npm run build' output per checkpoint (+ agent fixes)
```
