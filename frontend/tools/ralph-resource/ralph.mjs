#!/usr/bin/env zx
/* eslint-disable no-console */
/**
 * Ralph loop — autonomous, SEQUENTIAL, per-component migration of
 * `async ngOnInit()` data loading to `resource()` + signals for the CircaBC
 * Angular 22 frontend, written with google/zx.
 *
 * The "Ralph" technique (after Geoffrey Huntley) runs an AI agent in a loop:
 * each iteration does one small, independently verifiable unit of work, records
 * progress, and the loop continues until the backlog is empty. Components are
 * processed ONE AT A TIME (no parallelism).
 *
 * Primary goal: replace imperative async data loading performed in
 * `async ngOnInit()` with reactive `resource()` (wrapping the generated
 * `...Async` service methods) exposed through signals, so no component uses
 * `async ngOnInit` for fetching.
 *
 * Each iteration:
 *   1. asks `kiro-cli` (headless, sonnet-5 / xhigh effort / angular-developer
 *      skill) to convert just that component (see improve-prompt.md),
 *   2. verifies, in order:
 *        - `npm run tsc-go`        (fast whole-project type-check via tsgo)
 *        - `npm run lint-biome`    (Biome lint)
 *        - the changed component's spec: `npm run test:file -- <spec>`
 *        - `npm run build`         (Angular whole-app AOT template type-check)
 *   3. on ANY gate failure the component is NEVER reverted — instead the agent
 *      is asked to fix the code and the SAME gate is re-run, up to
 *      --fix-attempts times. If a gate still fails after all attempts the run
 *      stops (the migrated code is left in place for you to inspect).
 *   4. persists progress so the run is fully resumable.
 *
 * The `npm run build` gate runs for EVERY component — it is the only gate that
 * runs Angular's whole-app AOT template type-check, so it catches template/TS
 * signal-call mismatches (`{{ foo }}` vs `{{ foo() }}`) and errored
 * `resource.value()` reads that tsc-go / biome / a shallow spec can miss. On
 * failure the agent gets up to --fix-attempts scoped fixes and the build is
 * re-run. A redundant final build runs at the end as a safety net (disable with
 * --no-final-build).
 *
 * This script does NOT use git at all — no add / commit / checkout / push.
 * Successful migrations are left as uncommitted working-tree changes for you to
 * review and commit yourself; failed attempts are also left in place (the loop
 * never reverts anything).
 *
 * Usage:
 *   npx zx tools/ralph-resource/ralph.mjs --dry-run
 *   npx zx tools/ralph-resource/ralph.mjs --limit=1
 *   npx zx tools/ralph-resource/ralph.mjs --all
 *   npx zx tools/ralph-resource/ralph.mjs --status
 */
import { $, fs, glob, chalk, argv } from 'zx';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

$.verbose = false;

// ---------------------------------------------------------------------------
// Paths
// ---------------------------------------------------------------------------
const HERE = path.dirname(fileURLToPath(import.meta.url));
const FRONTEND = path.resolve(HERE, '..', '..'); // tools/ralph-resource -> tools -> frontend
const STATE_DIR = path.join(HERE, '.state');
const PROGRESS_FILE = path.join(STATE_DIR, 'progress.json');
const REPORT_FILE = path.join(STATE_DIR, 'report.md');
const LOG_DIR = path.join(STATE_DIR, 'logs');
const PROMPT_TEMPLATE = path.join(HERE, 'improve-prompt.md');

// The marker the migration targets: an `async ngOnInit` method.
const TARGET_RE = /async\s+ngOnInit\s*\(/;

// ---------------------------------------------------------------------------
// CLI options
// ---------------------------------------------------------------------------
const HELP = `Ralph loop (zx) — SEQUENTIAL per-component async-ngOnInit -> resource() migration

Options:
  --limit=N            Max components to process this run (default: 1)
  --all                Process every pending component
  --only=<substr>      Only components whose path contains <substr>
  --skip-lint          Skip the 'npm run lint-biome' gate
  --skip-test          Skip the changed component's spec gate
  --skip-build         Skip the per-component 'npm run build' gate (NOT recommended)
  --build-every=N      Also run a batch 'npm run build' every N migrations (default: 0 = off,
                       since every component is already build-verified)
  --no-final-build     Do not run the extra 'npm run build' at the end of the run
  --fix-attempts=N     Times the agent may retry fixing a failing gate before the
                       run stops (default: 3). Alias: --build-fix-attempts
  --build-timeout=<d>  Timeout for each 'npm run build', zx duration (default: 20m)
  --dry-run            Build/show the queue and plan; do not call the agent
  --print-prompt       With --dry-run, print the rendered prompt for the next comp
  --status             Print progress summary from the saved queue and exit
  --rebuild-queue      Recompute the queue from disk (discards saved statuses)
  --max-failures=N     Abort after N consecutive failures (default: 3)
  --model=<id>         Model passed to kiro-cli (default: claude-sonnet-5)
  --effort=<level>     Reasoning effort: low|medium|high|xhigh|max (default: xhigh)
  --skill=<name>       Skill the agent loads (default: angular-developer;
                       pass --no-skill to disable)
  --agent=<name>       kiro-cli agent to use (default: default agent)
  --agent-timeout=<d>  Per-component agent timeout, zx duration (default: 20m)
  -h, --help           Show this help

Gates (in order): npm run tsc-go -> npm run lint-biome -> the changed spec -> npm run build.
On ANY gate failure the component is NEVER reverted — the agent retries the fix
up to --fix-attempts times; if it still fails the run stops so you can inspect.
This script never uses git and never pushes. 'npm run build' (Angular whole-app
AOT template type-check) runs for EVERY component.

Examples:
  npx zx tools/ralph-resource/ralph.mjs --dry-run
  npx zx tools/ralph-resource/ralph.mjs --limit=1
  npx zx tools/ralph-resource/ralph.mjs --all`;

if (argv.help || argv.h) {
  console.log(HELP);
  process.exit(0);
}

const opts = {
  // Default: process EVERY pending (not-yet-migrated) component. Pass --limit=N
  // to cap a run, or --all explicitly (both resolve to the same "all" default).
  limit: argv.all ? Infinity : argv.limit !== undefined ? Number(argv.limit) : Infinity,
  only: argv.only ? String(argv.only) : null,
  skipLint: Boolean(argv['skip-lint'] ?? false),
  skipTest: Boolean(argv['skip-test'] ?? false),
  skipBuild: Boolean(argv['skip-build'] ?? false),
  buildEvery: argv['build-every'] !== undefined ? Number(argv['build-every']) : 0,
  finalBuild: argv['final-build'] !== false,
  fixAttempts:
    argv['fix-attempts'] !== undefined
      ? Number(argv['fix-attempts'])
      : argv['build-fix-attempts'] !== undefined
        ? Number(argv['build-fix-attempts'])
        : 3,
  buildTimeout: String(argv['build-timeout'] ?? '20m'),
  dryRun: Boolean(argv['dry-run'] ?? false),
  printPrompt: Boolean(argv['print-prompt'] ?? false),
  status: Boolean(argv.status ?? false),
  rebuildQueue: Boolean(argv['rebuild-queue'] ?? false),
  maxFailures: argv['max-failures'] !== undefined ? Number(argv['max-failures']) : 3,
  model: argv.model ? String(argv.model) : 'claude-sonnet-5',
  effort: argv.effort ? String(argv.effort) : 'xhigh',
  skill:
    argv.skill === undefined
      ? 'angular-developer'
      : argv.skill === false
        ? null
        : String(argv.skill),
  agent: argv.agent ? String(argv.agent) : null,
  agentTimeout: String(argv['agent-timeout'] ?? '20m'),
  trustAll: true,
};

// ---------------------------------------------------------------------------
// Small helpers
// ---------------------------------------------------------------------------
const q = (s) => `'` + String(s).replaceAll(`'`, `'\\''`) + `'`;

async function exists(p) {
  try {
    await fs.access(p);
    return true;
  } catch {
    return false;
  }
}

/** Run a shell command; never throws — returns {ok, code, stdout, stderr}. */
async function run(cmd, { cwd = FRONTEND, timeout } = {}) {
  const shell = timeout ? $({ cwd, nothrow: true, timeout }) : $({ cwd, nothrow: true });
  const res = await shell`bash -c ${cmd}`;
  return {
    ok: res.exitCode === 0,
    code: res.exitCode,
    stdout: res.stdout ?? '',
    stderr: res.stderr ?? '',
  };
}

function tail(s, n = 40) {
  return String(s).split('\n').slice(-n).join('\n');
}

function relatedFiles(componentRel) {
  const base = componentRel.replace(/\.ts$/, '');
  return ['.ts', '.html', '.scss', '.spec.ts'].map((ext) => base + ext);
}

async function existingRelated(componentRel) {
  const out = [];
  for (const f of relatedFiles(componentRel)) {
    if (await exists(path.join(FRONTEND, f))) out.push(f);
  }
  return out;
}

// ---------------------------------------------------------------------------
// Queue discovery / persistence — only components that still have async ngOnInit
// ---------------------------------------------------------------------------
async function discover() {
  const files = await glob('src/app/**/*.component.ts', {
    cwd: FRONTEND,
    ignore: ['**/*.spec.ts', 'src/app/core/generated/**'],
  });
  const items = [];
  for (const rel of files.sort()) {
    const src = await fs.readFile(path.join(FRONTEND, rel), 'utf8');
    if (!TARGET_RE.test(src)) continue; // only enqueue components needing migration
    items.push({ file: rel, status: 'pending', reason: 'has async ngOnInit', attempts: 0, result: '' });
  }
  return items;
}

async function loadOrBuildQueue() {
  let queue;
  if ((await exists(PROGRESS_FILE)) && !opts.rebuildQueue) {
    queue = JSON.parse(await fs.readFile(PROGRESS_FILE, 'utf8'));
    const known = new Set(queue.items.map((i) => i.file));
    for (const it of await discover()) if (!known.has(it.file)) queue.items.push(it);
  } else {
    queue = { createdAt: new Date().toISOString(), items: await discover() };
  }
  for (const it of queue.items) {
    if (it.status !== 'pending') continue;
    const abs = path.join(FRONTEND, it.file);
    if (!(await exists(abs))) {
      it.status = 'skipped';
      it.reason = 'file missing';
      continue;
    }
    if (!TARGET_RE.test(await fs.readFile(abs, 'utf8'))) {
      it.status = 'done';
      it.reason = 'no async ngOnInit';
    }
  }
  return queue;
}

async function saveQueue(queue) {
  await fs.mkdir(STATE_DIR, { recursive: true });
  const tmp = `${PROGRESS_FILE}.tmp`;
  await fs.writeFile(tmp, JSON.stringify(queue, null, 2));
  await fs.rename(tmp, PROGRESS_FILE);
}

function tally(queue) {
  const t = { total: queue.items.length, pending: 0, done: 0, failed: 0, blocked: 0, skipped: 0 };
  for (const it of queue.items) t[it.status] = (t[it.status] ?? 0) + 1;
  return t;
}

function printSummary(queue) {
  const t = tally(queue);
  console.log(
    chalk.bold('\nQueue: ') +
      `${t.total} components  ` +
      chalk.green(`${t.done} done`) +
      '  ' +
      chalk.yellow(`${t.pending} pending`) +
      '  ' +
      chalk.magenta(`${t.blocked} blocked`) +
      '  ' +
      chalk.red(`${t.failed} failed`) +
      '  ' +
      chalk.gray(`${t.skipped} skipped`)
  );
}

async function writeReport(queue) {
  const t = tally(queue);
  const lines = [
    '# Ralph loop report — async ngOnInit -> resource()',
    '',
    `- Generated: ${new Date().toISOString()}`,
    `- Total components in queue: ${t.total}`,
    `- Done (no async ngOnInit): ${t.done}`,
    `- Pending: ${t.pending}`,
    `- Blocked: ${t.blocked}`,
    `- Failed: ${t.failed}`,
    `- Skipped: ${t.skipped}`,
    '',
  ];
  const notable = queue.items.filter((i) => i.status === 'failed' || i.status === 'blocked');
  if (notable.length) {
    lines.push('## Needs attention', '');
    for (const it of notable) lines.push(`- \`${it.file}\` — **${it.status}** — ${it.result || it.reason}`);
    lines.push('');
  }
  await fs.mkdir(STATE_DIR, { recursive: true });
  await fs.writeFile(REPORT_FILE, lines.join('\n'));
}

// ---------------------------------------------------------------------------
// Prompt + agent
// ---------------------------------------------------------------------------
function skillBlock() {
  if (!opts.skill) return '';
  const base = `~/.kiro/skills/${opts.skill}/SKILL.md`;
  if (opts.skill === 'angular-developer') {
    return [
      `## Use the angular-developer skill`,
      `Load and follow the **angular-developer** skill at \`${base}\` and apply its`,
      `Angular 22 best practices. For this migration the most relevant references`,
      `(read as needed) are:`,
      `- \`references/resource.md\` — async reactivity with \`resource()\` (PRIMARY)`,
      `- \`references/signals-overview.md\`, \`references/linked-signal.md\`, \`references/effects.md\``,
      `- \`references/components.md\` — component anatomy & @if/@for/@switch`,
      `- \`references/inputs.md\`, \`references/outputs.md\``,
      `- \`references/di-fundamentals.md\`, \`references/injection-context.md\``,
      `- \`references/testing-fundamentals.md\`, \`references/component-harnesses.md\` (when you touch the spec)`,
      `The orchestrator verifies with \`npm run tsc-go\`, \`npm run lint-biome\`, and`,
      `this component's spec — you do NOT need to run \`ng build\` yourself.`,
      ``,
    ].join('\n');
  }
  return [`## Use the ${opts.skill} skill`, `Load and follow the **${opts.skill}** skill at \`${base}\`.`, ``].join('\n');
}

async function buildPrompt(componentRel) {
  const tmpl = await fs.readFile(PROMPT_TEMPLATE, 'utf8');
  const related = await existingRelated(componentRel);
  return tmpl
    .replaceAll('{{SKILL_BLOCK}}', skillBlock())
    .replaceAll('{{COMPONENT_PATH}}', componentRel)
    .replaceAll('{{RELATED_FILES}}', related.map((f) => '`' + f + '`').join(', ') || 'none');
}

function parseResult(stdout) {
  const m = stdout.match(/RALPH_RESULT:\s*(.+)/);
  return m ? m[1].trim() : '';
}

async function runAgent(prompt, logFile) {
  const args = ['chat', '--no-interactive'];
  if (opts.trustAll) args.push('--trust-all-tools');
  if (opts.agent) args.push('--agent', opts.agent);
  if (opts.model) args.push('--model', opts.model);
  if (opts.effort) args.push('--effort', opts.effort);
  args.push(prompt);
  const res = await $({ cwd: FRONTEND, nothrow: true, timeout: opts.agentTimeout })`kiro-cli ${args}`;
  await fs.mkdir(LOG_DIR, { recursive: true });
  await fs.appendFile(
    logFile,
    `$ kiro-cli ${args.slice(0, -1).join(' ')} <prompt>\n\n${res.stdout ?? ''}\n----- STDERR -----\n${res.stderr ?? ''}\n`
  );
  return { ok: res.exitCode === 0, code: res.exitCode, stdout: res.stdout ?? '' };
}

// ---------------------------------------------------------------------------
// No git, no revert.
//
// Successful migrations are simply LEFT in the working tree (uncommitted) for
// the user to review and commit themselves. On ANY gate failure we do NOT
// revert — the agent is asked to fix the code and the gate is re-run (see
// gateFixPrompt / gateWithFix / runBuildWithFix), up to opts.fixAttempts times.
// If a gate is still red after all attempts the run stops and the code is left
// exactly as-is for manual inspection.
// ---------------------------------------------------------------------------
function gateFixPrompt(gateName, componentRel, related, errorTail) {
  return [
    `An automated "Ralph loop" is migrating CircaBC Angular 22 components from`,
    `\`async ngOnInit()\` data loading to reactive \`resource()\` + signals, one at a`,
    `time. The component just migrated is:`,
    ``,
    `    ${componentRel}`,
    ``,
    `Its related files: ${related.map((f) => '`' + f + '`').join(', ') || 'none'}.`,
    ``,
    `The **${gateName}** gate is currently FAILING. Do NOT revert or undo the`,
    `resource()/signals migration — instead fix the code so the gate passes.`,
    ``,
    skillBlock(),
    `## Your task`,
    `Diagnose and fix the failure shown below with the SMALLEST possible change.`,
    `Common causes after a resource() migration:`,
    `- a field converted to a signal/resource value in the \`.ts\` but still read`,
    `  without \`()\` in its \`.html\` (or the reverse) — e.g. \`{{ foo }}\` must become`,
    `  \`{{ foo() }}\`;`,
    `- a template reading \`resource.value()\` while the resource can be in the ERROR`,
    `  state (\`.value()\` THROWS on error) — guard with \`hasValue()\` or return a safe`,
    `  default from the loader;`,
    `- a \`@for\` missing a \`track\` expression;`,
    `- a spec that still expects the old imperative data-loading behavior — update it`,
    `  to drive the resource() + signals instead.`,
    ``,
    `## Constraints`,
    `- Make minimal, targeted edits — fix only what the gate reports; do NOT reformat`,
    `  untouched lines or refactor unrelated code.`,
    `- Do NOT reintroduce \`async ngOnInit\` data loading and do NOT change the`,
    `  component's public API (selector, input/output names & types).`,
    `- Do NOT run the gate command or ANY git command yourself; the orchestrator`,
    `  re-runs the gate to verify your fix.`,
    ``,
    `## ${gateName} output (tail)`,
    '```',
    errorTail,
    '```',
    ``,
    `End your response with exactly one line prefixed literally with \`RALPH_RESULT:\``,
    `summarizing what you changed.`,
  ].join('\n');
}

/**
 * Run a gate command; on failure ask the agent to fix the code and re-run the
 * same gate, up to opts.fixAttempts times. NEVER reverts. Returns
 * {ok, fixed?, resultLine?, detail?}.
 */
async function gateWithFix({ gateName, cmd, timeout, componentRel, logFile }) {
  const related = await existingRelated(componentRel);
  const runIt = () => run(cmd, timeout ? { timeout } : {});

  let r = await runIt();
  if (r.ok) return { ok: true };

  for (let attempt = 1; attempt <= opts.fixAttempts; attempt++) {
    process.stdout.write(`fix ${attempt}/${opts.fixAttempts}… `);
    const safeGate = gateName.replace(/\W+/g, '_');
    const fixLog = path.join(LOG_DIR, `${path.basename(logFile, '.log')}-${safeGate}-fix${attempt}.log`);
    const agent = await runAgent(gateFixPrompt(gateName, componentRel, related, tail(r.stdout + r.stderr, 200)), fixLog);
    const resultLine = parseResult(agent.stdout);
    r = await runIt();
    if (r.ok) return { ok: true, fixed: true, resultLine };
    process.stdout.write(chalk.red('still failing') + ' ');
  }
  return { ok: false, detail: tail(r.stdout + r.stderr) };
}

// ---------------------------------------------------------------------------
// Verification gates: tsc-go -> lint-biome -> changed spec.
// Each gate self-heals via the agent (no revert, ever).
// ---------------------------------------------------------------------------
async function verify(componentRel, logFile) {
  // 1) Fast whole-project type-check with the tsgo engine.
  let g = await gateWithFix({ gateName: 'tsc-go', cmd: 'npm run tsc-go', componentRel, logFile });
  if (!g.ok) return { ok: false, gate: 'tsc-go', detail: g.detail };

  // 2) Biome lint.
  if (!opts.skipLint) {
    g = await gateWithFix({ gateName: 'lint-biome', cmd: 'npm run lint-biome', componentRel, logFile });
    if (!g.ok) return { ok: false, gate: 'lint-biome', detail: g.detail };
  }

  // 3) The changed component's spec (also compiles its template via ng build).
  if (!opts.skipTest) {
    const spec = componentRel.replace(/\.ts$/, '.spec.ts');
    if (await exists(path.join(FRONTEND, spec))) {
      const name = `test (${path.basename(spec)})`;
      g = await gateWithFix({ gateName: name, cmd: `npm run test:file -- ${q(spec)}`, timeout: '10m', componentRel, logFile });
      if (!g.ok) return { ok: false, gate: name, detail: g.detail };
    }
  }
  return { ok: true };
}

// ---------------------------------------------------------------------------
// Full production build checkpoint (npm run build) + agent-driven fix.
// This is the only gate that runs Angular's whole-app AOT template
// type-checking, so it catches template<->TS signal-call mismatches that
// tsc-go / biome / a shallow spec can miss. Never reverts, never uses git.
// ---------------------------------------------------------------------------
function buildFixPrompt(errorTail) {
  return [
    `An automated "Ralph loop" has been migrating CircaBC Angular 22 components from`,
    `\`async ngOnInit()\` data loading to reactive \`resource()\` + signals, one at a`,
    `time. Each migrated component already passed \`npm run tsc-go\`,`,
    `\`npm run lint-biome\`, and its own spec, but the full production build`,
    `\`npm run build\` (\`ng build\`, which runs Angular's whole-app AOT template`,
    `type-checking) now FAILS.`,
    ``,
    skillBlock(),
    `## Your task`,
    `Diagnose and fix the build errors shown below with the SMALLEST possible change.`,
    `Do NOT revert or undo any migration. They are almost always a side effect of the`,
    `resource() migrations, most commonly:`,
    `- a field converted to a signal/resource value in the \`.ts\` but still read`,
    `  without \`()\` in its \`.html\` (or the reverse) — e.g. \`{{ foo }}\` must become`,
    `  \`{{ foo() }}\`;`,
    `- a template reading \`resource.value()\` while the resource can be in the ERROR`,
    `  state (\`.value()\` THROWS on error) — guard with \`hasValue()\` or catch inside`,
    `  the loader and return a safe default;`,
    `- a \`@for\` missing a \`track\` expression after a control-flow tweak.`,
    ``,
    `## Constraints`,
    `- Make minimal, targeted edits — fix only what the build reports; do NOT reformat`,
    `  untouched lines or refactor unrelated code.`,
    `- Do NOT change any component's public API (selector, input/output names and`,
    `  types, exported symbols) and do NOT reintroduce \`async ngOnInit\` data loading.`,
    `  Keep the resource()/signals migration and make the build green by fixing the`,
    `  source of each error.`,
    `- Do NOT run \`npm run build\`, \`ng build\`, \`ng test\`, or ANY git command`,
    `  yourself. The orchestrator re-runs \`npm run build\` to verify your fix.`,
    ``,
    `## Build output (tail)`,
    '```',
    errorTail,
    '```',
    ``,
    `End your response with exactly one line prefixed literally with \`RALPH_RESULT:\``,
    `summarizing what you changed (e.g.`,
    `\`RALPH_RESULT: guarded 2 template value() reads with hasValue() in events + members\`).`,
  ].join('\n');
}

/**
 * Core: run `npm run build`; on failure ask the agent to fix it (up to
 * opts.fixAttempts) and rebuild. Does NOT revert — the caller decides what to do
 * with the result. Returns {ok, fixed?, detail?}.
 */
async function runBuildWithFix(label) {
  await fs.mkdir(LOG_DIR, { recursive: true });
  const logFile = path.join(LOG_DIR, `build-${label}.log`);

  let r = await run('npm run build', { timeout: opts.buildTimeout });
  await fs.writeFile(logFile, `$ npm run build\n\n${r.stdout}\n----- STDERR -----\n${r.stderr}`);
  if (r.ok) return { ok: true };

  for (let attempt = 1; attempt <= opts.fixAttempts; attempt++) {
    process.stdout.write(`fix ${attempt}/${opts.fixAttempts}… `);
    const fixLog = path.join(LOG_DIR, `build-${label}-fix${attempt}.log`);
    const agent = await runAgent(buildFixPrompt(tail(r.stdout + r.stderr, 200)), fixLog);
    const resultLine = parseResult(agent.stdout);
    r = await run('npm run build', { timeout: opts.buildTimeout });
    await fs.appendFile(
      logFile,
      `\n----- BUILD AFTER FIX ${attempt} (agent exit ${agent.code}) -----\n${resultLine}\n${tail(r.stdout + r.stderr)}\n`
    );
    if (r.ok) return { ok: true, fixed: true, resultLine };
    process.stdout.write(chalk.red('still failing') + ' ');
  }
  return { ok: false, detail: tail(r.stdout + r.stderr) };
}

/**
 * Per-component build gate: the strongest gate, run after every conversion so a
 * component that breaks Angular's whole-app AOT template type-check (e.g. a
 * `{{ foo }}` that must become `{{ foo() }}`, or a `resource.value()` read that
 * can throw in the error state) is caught immediately for THIS component. On
 * failure the agent gets up to --fix-attempts scoped fixes and the build is
 * re-run; nothing is reverted or committed. Returns {ok, detail?}.
 */
async function componentBuildGate(componentRel) {
  process.stdout.write('  build… ');
  const stamp = componentRel.replaceAll('/', '_').replace(/\.ts$/, '');
  const res = await runBuildWithFix(`comp-${stamp}`);
  if (res.ok) {
    console.log(res.fixed ? chalk.green('fixed') : chalk.green('passed'));
    if (res.resultLine) console.log('    ' + chalk.gray(res.resultLine));
  } else {
    console.log(chalk.red('FAILED'));
  }
  return res;
}

/**
 * Periodic / final full-build checkpoint (npm run build) + agent-driven fix.
 * Used only when --build-every > 0 or for the final safety build. Never reverts
 * and never uses git — a successful fix is simply left in the working tree.
 */
async function buildCheckpoint(label) {
  process.stdout.write(`  build (${label})… `);
  const res = await runBuildWithFix(label);
  if (res.ok) {
    console.log(res.fixed ? chalk.green('fixed') : chalk.green('passed'));
    if (res.resultLine) console.log('      ' + chalk.gray(res.resultLine));
    return { ok: true, fixed: res.fixed };
  }
  console.log(chalk.red('still failing'));
  return { ok: false, detail: res.detail };
}

async function abortOnBuild(queue, detail) {
  await saveQueue(queue);
  await writeReport(queue);
  console.error(
    chalk.red(`\nAborted: 'npm run build' still fails after ${opts.fixAttempts} fix attempt(s).`)
  );
  console.error(
    chalk.gray(
      `All migrated code is left in the working tree (never reverted, never committed).\n` +
        `Inspect the build log under ${path.relative(FRONTEND, LOG_DIR)}/, fix manually,\n` +
        `then re-run the same command to resume.`
    )
  );
  if (detail) console.error(chalk.gray('\n' + detail));
  process.exit(1);
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------
async function main() {
  if (!(await exists(PROMPT_TEMPLATE))) {
    console.error(chalk.red(`Missing prompt template: ${PROMPT_TEMPLATE}`));
    process.exit(1);
  }

  const queue = await loadOrBuildQueue();
  await saveQueue(queue);

  if (opts.status) {
    printSummary(queue);
    const failing = queue.items.filter((i) => i.status === 'failed' || i.status === 'blocked');
    if (failing.length) {
      console.log(chalk.bold('\nNeeds attention:'));
      for (const it of failing) console.log(`  ${chalk.red(it.status)}  ${it.file} — ${it.result || it.reason}`);
    }
    return;
  }

  let candidates = queue.items.filter((i) => i.status === 'pending');
  if (opts.only) candidates = candidates.filter((i) => i.file.includes(opts.only));
  const limit = opts.limit === Infinity ? candidates.length : opts.limit;

  printSummary(queue);
  console.log(
    chalk.bold(`\nPlan: `) +
      `${Math.min(candidates.length, limit)} of ${candidates.length} pending, one at a time ` +
      chalk.gray(
        `(gates: tsc-go${opts.skipLint ? '' : ' -> lint-biome'}${opts.skipTest ? '' : ' -> spec'}${opts.skipBuild ? '' : ' -> build'}; no git; fix attempts: ${opts.fixAttempts})`
      )
  );
  console.log(
    chalk.gray(`      agent: kiro-cli • model: ${opts.model} • effort: ${opts.effort} • skill: ${opts.skill ?? 'none'}`)
  );
  if (opts.skipBuild) {
    console.log(chalk.gray('      build: disabled (--skip-build) — NOT recommended'));
  } else {
    const extra = opts.buildEvery > 0 ? ` + batch every ${opts.buildEvery}` : '';
    console.log(
      chalk.gray(
        `      build: npm run build per component${extra}${opts.finalBuild ? ' + at end' : ''} • fix attempts: ${opts.fixAttempts}`
      )
    );
  }
  console.log(chalk.gray('      failure policy: never revert — retry-to-fix, then stop for manual inspection'));

  if (opts.dryRun) {
    console.log(chalk.bold('\nNext up:'));
    for (const it of candidates.slice(0, limit === Infinity ? 20 : Math.min(limit, 20)))
      console.log(`  ${chalk.yellow('•')} ${it.file}  ${chalk.gray(`(${it.reason})`)}`);
    if (opts.printPrompt && candidates.length) {
      console.log(chalk.bold(`\n----- rendered prompt for ${candidates[0].file} -----`));
      console.log(await buildPrompt(candidates[0].file));
      console.log(chalk.bold('----- end prompt -----'));
    }
    console.log(chalk.gray('\nDry run — no agent calls, no file changes.'));
    await writeReport(queue);
    return;
  }

  if (candidates.length === 0) {
    console.log(chalk.green('\nNothing to do — no pending components match.'));
    await writeReport(queue);
    return;
  }

  let processed = 0;
  let consecutiveFailures = 0;
  let doneThisRun = 0;
  let sinceLastBuild = 0;

  for (const item of candidates) {
    if (processed >= limit) break;
    processed += 1;
    item.attempts += 1;

    const label = `[${processed}/${Math.min(candidates.length, limit)}]`;
    console.log(chalk.bold(`\n${label} `) + item.file);
    const stamp = item.file.replaceAll('/', '_').replace(/\.ts$/, '');
    const logFile = path.join(LOG_DIR, `${stamp}.log`);
    await fs.mkdir(LOG_DIR, { recursive: true });

    // 1) Agent converts just this component. If the migration is not actually
    //    completed, retry the conversion (never revert) up to --fix-attempts.
    process.stdout.write('  agent… ');
    let agent = await runAgent(await buildPrompt(item.file), logFile);
    let resultLine = parseResult(agent.stdout);
    console.log(agent.ok ? chalk.green('done') : chalk.red(`exit ${agent.code}`));
    if (resultLine) console.log('  ' + chalk.gray(resultLine));

    if (/RALPH[_ ]?blocked/i.test(resultLine)) {
      // Agent judged the component unsuitable (e.g. ngOnInit does only side
      // effects, not data loading); it left the code as-is.
      item.status = 'blocked';
      item.result = resultLine;
      consecutiveFailures = 0;
      await saveQueue(queue);
      continue;
    }

    // Retry the conversion until async ngOnInit is gone. No revert, ever.
    let convAttempt = 0;
    while (
      convAttempt < opts.fixAttempts &&
      (!agent.ok || TARGET_RE.test(await fs.readFile(path.join(FRONTEND, item.file), 'utf8')))
    ) {
      convAttempt += 1;
      process.stdout.write(`  retry ${convAttempt}/${opts.fixAttempts}… `);
      agent = await runAgent(await buildPrompt(item.file), logFile);
      resultLine = parseResult(agent.stdout) || resultLine;
      console.log(agent.ok ? chalk.green('done') : chalk.red(`exit ${agent.code}`));
    }

    const stillHasTarget = TARGET_RE.test(await fs.readFile(path.join(FRONTEND, item.file), 'utf8'));
    if (!agent.ok || stillHasTarget) {
      item.status = 'failed';
      item.result = stillHasTarget
        ? `async ngOnInit still present after ${opts.fixAttempts} attempt(s) (left in place); see ${path.relative(FRONTEND, logFile)}`
        : `agent exited ${agent.code} after ${opts.fixAttempts} attempt(s) (left in place); see ${path.relative(FRONTEND, logFile)}`;
      consecutiveFailures += 1;
      await saveQueue(queue);
      // The working tree may now be half-migrated; stop rather than run
      // whole-project gates on top of it. Nothing is reverted.
      return stop(queue, item);
    }

    // 2) Verify with self-healing gates: tsc-go -> lint-biome -> spec.
    process.stdout.write('  verify… ');
    const v = await verify(item.file, logFile);
    if (!v.ok) {
      console.log(chalk.red(`FAILED at ${v.gate}`));
      await fs.appendFile(logFile, `\n----- GATE FAILED (${v.gate}) after ${opts.fixAttempts} fix attempt(s) -----\n${v.detail}\n`);
      item.status = 'failed';
      item.result = `${v.gate} still failing after ${opts.fixAttempts} fix attempt(s) (left in place); see ${path.relative(FRONTEND, logFile)}`;
      consecutiveFailures += 1;
      await saveQueue(queue);
      // Never revert; stop so the user can inspect rather than cascade the
      // failure into later whole-project gates.
      return stop(queue, item);
    }
    console.log(chalk.green('passed'));

    // 2b) Per-component build gate (default ON): Angular's whole-app AOT
    //     template type-check. Self-heals via the agent; never reverts.
    if (!opts.skipBuild) {
      const b = await componentBuildGate(item.file);
      if (!b.ok) {
        await fs.appendFile(logFile, `\n----- BUILD GATE FAILED after ${opts.fixAttempts} fix attempt(s) -----\n${b.detail || ''}\n`);
        item.status = 'failed';
        item.result = `npm run build still failing after ${opts.fixAttempts} fix attempt(s) (left in place); see ${path.relative(FRONTEND, logFile)}`;
        consecutiveFailures += 1;
        await saveQueue(queue);
        return stop(queue, item);
      }
      sinceLastBuild = 0; // this component is already build-verified
    }

    // 3) Success — leave the migrated files uncommitted in the working tree for
    //    review. No git operations of any kind.
    item.status = 'done';
    item.result = resultLine || 'migrated async ngOnInit to resource()';
    consecutiveFailures = 0;
    doneThisRun += 1;
    sinceLastBuild += 1;
    await saveQueue(queue);

    // Optional periodic full-build checkpoint (off by default now that every
    // component is build-verified; only runs if --build-every > 0 is passed).
    if (!opts.skipBuild && opts.buildEvery > 0 && sinceLastBuild >= opts.buildEvery) {
      const res = await buildCheckpoint(`after-${doneThisRun}`);
      sinceLastBuild = 0;
      if (!res.ok) return abortOnBuild(queue, res.detail);
    }
  }

  // Final full-build checkpoint at the end of the run (unless we just built and
  // nothing changed since, or builds are disabled).
  if (!opts.skipBuild && opts.finalBuild && doneThisRun > 0 && sinceLastBuild > 0) {
    const res = await buildCheckpoint('final');
    if (!res.ok) return abortOnBuild(queue, res.detail);
  }

  await writeReport(queue);
  printSummary(queue);
  console.log(chalk.gray(`\nReport: ${path.relative(FRONTEND, REPORT_FILE)} • Logs: ${path.relative(FRONTEND, LOG_DIR)}/`));
}

/**
 * Stop the run because a component could not be made green after all fix
 * attempts. Nothing is reverted and nothing is committed — the code is left in
 * the working tree exactly as-is for manual inspection. Fully resumable.
 */
async function stop(queue, item) {
  await saveQueue(queue);
  await writeReport(queue);
  console.error(
    chalk.red(`\nStopped: ${item.file} could not be made green after ${opts.fixAttempts} fix attempt(s).`)
  );
  console.error(
    chalk.gray(
      `Nothing was reverted or committed — the migrated code is left in your working tree.\n` +
        `Inspect the logs under ${path.relative(FRONTEND, LOG_DIR)}/, fix manually, then re-run to resume.`
    )
  );
  process.exit(1);
}

await main();
