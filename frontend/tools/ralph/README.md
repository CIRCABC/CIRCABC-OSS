# Ralph loop — per-component improvement for the CircaBC frontend

An autonomous [Ralph loop](https://ghuntley.com/ralph/) (after Geoffrey Huntley)
implemented with [google/zx](https://github.com/google/zx). It grinds through
the frontend one component at a time, using `kiro-cli` headless to apply a
focused improvement, then verifies and (optionally) commits each change.

## Why — the improvement this drives

The frontend is already modern (signal `input()`/`output()`, `computed`,
`inject()`, standalone components, `@if`/`@for` control flow, `DestroyRef`
cleanup). One large, mechanical, high-value gap remains:

| Signal / pattern                              | Status in `src/app` |
| --------------------------------------------- | ------------------- |
| Components total                              | ~282                |
| On `ChangeDetectionStrategy.OnPush`           | **3**               |
| On `ChangeDetectionStrategy.Eager` (= legacy Default) | **~269**    |
| Legacy `*ngIf/*ngFor/*ngSwitch` templates     | 1                   |
| Legacy `@Input()` / `@Output()`               | 3 / 3               |
| Legacy `@ViewChild` family                    | 0                   |

`Eager` is this Angular version's name for the old `Default` strategy, so the
overwhelming majority of components still check on every tick. Because the app
is already signal-heavy, most are ready for **OnPush**, which is the primary
objective of this loop. Secondary, only-if-trivially-safe cleanups (constructor
→ `inject()`, remaining `*ngIf`/`*ngFor`, decorator inputs → signal inputs,
dead code) are also in the prompt.

> Complementary follow-up (not done by this loop): enable
> `@angular-eslint/prefer-on-push-component-change-detection` in
> `eslint.config.mjs` so new components start on OnPush and there is no
> regression once the backlog is cleared.

## How it works

Each iteration:

1. Pick the next `pending` `*.component.ts` from the persisted queue
   (`.state/progress.json`), skipping specs and `core/generated/**`.
2. Run `kiro-cli chat --no-interactive --trust-all-tools` with
   [`improve-prompt.md`](./improve-prompt.md), scoped to that one component.
3. Run verification gates in order: `npm run tsc-go` (fast whole-project
   type-check via tsgo), `npm run lint-biome` (Biome on `./src`), and the
   changed component's `.spec.ts` via `npm run test:file`.
4. On success: mark `done` and optionally `git commit`. On failure: `git
   checkout --` the component's files (revert) and mark `failed`. If the agent
   reports it cannot safely migrate, mark `blocked` (a `// TODO(ralph):` note is
   left in the file).
5. Persist progress after every component, so the run is fully resumable and
   safe to Ctrl-C.
6. After every `--build-every` successful migrations (default 10) and once more
   at the end of the run, run the full production build `npm run build`
   (`ng build`). This is the only gate that runs Angular's **whole-app AOT
   template type-checking**, so it catches template↔TS signal-call mismatches
   (`{{ foo }}` vs `{{ foo() }}`) that `tsc-go`, Biome, and a shallow spec can
   miss. If the build fails, the agent is asked to fix the errors (up to
   `--build-fix-attempts` times) and the build is re-run; if it still fails the
   run aborts so you can inspect `.state/logs/build-*.log`. With `--commit`, a
   successful fix is captured as a single `fix(build): …` commit.

## Requirements

- `kiro-cli` on `PATH` (headless mode)
- Node + the repo's dev deps installed (`zx` is already in `devDependencies`)
- Run from the `frontend/` directory

## Usage — running the loop

Run everything from the `frontend/` directory, with `kiro-cli` on your `PATH`.
Each invocation of the script **is** the loop: it walks the queue one component
at a time until it reaches `--limit`, runs out of pending components, or aborts
after `--max-failures` consecutive failures. There is no separate "start"
command — you drive it with `--limit`/`--all` and re-run to resume.

### 1. First runs (safe, incremental)

```bash
# (recommended) work on a dedicated branch so a batch is easy to review or drop
git switch -c chore/ralph-onpush

# See the queue and what would run — no agent calls, no file changes
npx zx tools/ralph/ralph.mjs --dry-run

# (optional) print the exact prompt the agent will get for the next component
npx zx tools/ralph/ralph.mjs --dry-run --print-prompt

# Do ONE component end-to-end, without committing (recommended first real run)
npx zx tools/ralph/ralph.mjs --limit=1

# A reviewable batch, committing each success (never pushes)
npx zx tools/ralph/ralph.mjs --limit=25 --commit
```

### 2. Run the whole backlog

```bash
# Process every pending component, committing each success. Long-running.
npx zx tools/ralph/ralph.mjs --all --commit
```

The run is **fully resumable**: progress is saved after every component in
`.state/progress.json`. If it aborts (e.g. 3 consecutive failures) or you
Ctrl-C, fix the cause and re-run the **same command** — it continues the
backlog and skips anything already `done`.

```bash
# Resume after an abort or interruption — identical command, picks up where it left off
npx zx tools/ralph/ralph.mjs --all --commit

# Keep re-running until the backlog is empty, pausing for you on repeated failures
until npx zx tools/ralph/ralph.mjs --all --commit; do
  echo "Ralph aborted — inspect .state/report.md, fix the cause, then press Enter to resume"
  read _
done
```

### 3. Scope, monitor, reset

```bash
# Restrict the loop to components whose path contains a substring
npx zx tools/ralph/ralph.mjs --only=library --commit

# Check progress at any time (also lists anything failed/blocked)
npx zx tools/ralph/ralph.mjs --status

# Recompute the queue from disk, discarding saved statuses
npx zx tools/ralph/ralph.mjs --rebuild-queue --dry-run
```

### Options

| Option                | Default             | Meaning                                                    |
| --------------------- | ------------------- | ---------------------------------------------------------- |
| `--limit=N`           | `1`                 | Max components this run                                    |
| `--all`               | —                   | Process every pending component                            |
| `--only=<substr>`     | —                   | Restrict to paths containing `<substr>`                    |
| `--commit`            | off                 | Commit each success (respects git hooks; **never pushes**) |
| `--dry-run`           | —                   | Build/show the queue and plan only; no agent calls         |
| `--print-prompt`      | —                   | With `--dry-run`, print the rendered prompt for the next   |
| `--status`            | —                   | Print progress summary and exit                            |
| `--rebuild-queue`     | —                   | Recompute the queue from disk (discards saved statuses)    |
| `--skip-lint`         | off                 | Skip the `npm run lint-biome` gate                         |
| `--skip-test`         | off                 | Skip the changed component's spec gate                     |
| `--build-every=N`     | `10`                | Run full `npm run build` after every N migrations (`0` = off) |
| `--no-final-build`    | —                   | Skip the extra `npm run build` at the end of the run       |
| `--skip-build`        | off                 | Disable the periodic **and** final `npm run build` entirely |
| `--build-fix-attempts=N` | `2`              | Agent attempts to fix a failing build before aborting      |
| `--build-timeout=<d>` | `20m`               | Timeout for each `npm run build` (zx duration)             |
| `--max-failures=N`    | `3`                 | Abort after N consecutive failures                         |
| `--model=<id>`        | `claude-opus-4.8`   | Model passed to `kiro-cli`                                 |
| `--effort=<level>`    | `high`              | Reasoning effort: `low\|medium\|high\|xhigh\|max`           |
| `--skill=<name>`      | `angular-developer` | Skill loaded via the prompt (`--no-skill` to disable)      |
| `--agent=<name>`      | default agent       | `kiro-cli` agent to use                                    |
| `--agent-timeout=<d>` | `20m`               | Per-component agent timeout (zx duration)                  |
| `-h`, `--help`        | —                   | Show help                                                  |

Each iteration invokes:

```bash
kiro-cli chat --no-interactive --trust-all-tools \
  --model claude-opus-4.8 --effort high "<scoped prompt>"
```

and the prompt instructs the agent to load and follow the **angular-developer**
skill (`~/.kiro/skills/angular-developer/SKILL.md`) and its Angular 22
references (`components.md`, `signals-overview.md`, `inputs.md`, `outputs.md`,
`effects.md`, `di-fundamentals.md`, …). There is no `--skill` flag on
`kiro-cli`; the skill is loaded via the prompt.

## Safety

- **No pushes**, ever. Commits are **opt-in** via `--commit`.
- One component per iteration → small, reviewable diffs.
- Every change passes `tsc-go` + `lint-biome` + the component spec before it is
  kept; failures are auto-reverted so the tree never holds a broken component.
- A full `npm run build` runs every `--build-every` migrations and at the end,
  adding whole-app AOT template type-checking on top of the per-component gates.
  If it fails, the agent fixes it and rebuilds; if it still fails the run aborts
  (migrated components are preserved) so nothing is silently left broken.
- Only the target component's own files are edited (enforced by the prompt).
- Fully resumable: state lives in `.state/progress.json`; re-running continues
  where it left off. `--status` shows the summary; `.state/report.md` lists
  anything `failed`/`blocked` for manual attention.
- Recommended: run on a dedicated branch so a batch is easy to review or drop.

## State & output (git-ignored)

```
tools/ralph/.state/
  progress.json     # the resumable queue with per-component status
  report.md         # summary + items needing attention
  logs/*.log        # full agent transcript per component
  logs/build-*.log  # full 'npm run build' output per checkpoint (+ agent fixes)
```
