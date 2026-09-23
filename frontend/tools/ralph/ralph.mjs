#!/usr/bin/env zx
/* eslint-disable no-console */
/**
 * Ralph loop — autonomous, SEQUENTIAL, per-component code improvement for the
 * CircaBC Angular frontend, written with google/zx.
 *
 * The "Ralph" technique (after Geoffrey Huntley) runs an AI agent in a loop:
 * each iteration does one small, independently verifiable unit of work, records
 * progress, and the loop continues until the backlog is empty. Components are
 * processed ONE AT A TIME (no parallelism).
 *
 * Each iteration:
 *   1. asks `kiro-cli` (headless, opus-4.8 / high effort / angular-developer
 *      skill) to improve just that component
 *      (primary goal: migrate ChangeDetectionStrategy.Eager -> OnPush),
 *   2. verifies, in order:
 *        - `npm run tsc-go`        (fast whole-project type-check via tsgo)
 *        - `npm run lint-biome`    (Biome lint)
 *        - the changed component's spec: `npm run test:file -- <spec>`
 *   3. if all gates pass AND --commit is set, commits just that component's
 *      files; otherwise (on any failure) reverts the component with
 *      `git checkout --` so the tree never holds a broken component.
 *   4. persists progress so the run is fully resumable.
 *
 * Additionally, after every N successful migrations (default 10) and once more
 * at the end of the run, it runs the full production build `npm run build`
 * (`ng build`, i.e. Angular's whole-app AOT template type-check — the only gate
 * that catches template/TS signal-call mismatches app-wide). If the build
 * fails it asks the agent to fix the errors (up to --build-fix-attempts times)
 * and rebuilds; if it still fails the run aborts for manual inspection.
 *
 * This script NEVER pushes. It only ever runs `git add` / `git commit` /
 * `git checkout --` on the target component's own files (plus, when a periodic
 * build fix is needed and --commit is set, a single `fix(build): …` commit).
 *
 * Usage:
 *   npx zx tools/ralph/ralph.mjs --dry-run
 *   npx zx tools/ralph/ralph.mjs --limit=1 --commit
 *   npx zx tools/ralph/ralph.mjs --all --commit
 *   npx zx tools/ralph/ralph.mjs --status
 */
import { $, fs, glob, chalk, argv } from 'zx';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

$.verbose = false;

// ---------------------------------------------------------------------------
// Paths
// ---------------------------------------------------------------------------
const HERE = path.dirname(fileURLToPath(import.meta.url));
const FRONTEND = path.resolve(HERE, '..', '..'); // tools/ralph -> tools -> frontend
const STATE_DIR = path.join(HERE, '.state');
const PROGRESS_FILE = path.join(STATE_DIR, 'progress.json');
const REPORT_FILE = path.join(STATE_DIR, 'report.md');
const LOG_DIR = path.join(STATE_DIR, 'logs');
const PROMPT_TEMPLATE = path.join(HERE, 'improve-prompt.md');

// ---------------------------------------------------------------------------
// CLI options
// ---------------------------------------------------------------------------
const HELP = `Ralph loop (zx) — SEQUENTIAL per-component OnPush migration

Options:
  --limit=N            Max components to process this run (default: 1)
  --all                Process every pending component
  --only=<substr>      Only components whose path contains <substr>
  --commit             Commit each successful component (never pushes)
  --skip-lint          Skip the 'npm run lint-biome' gate
  --skip-test          Skip the changed component's spec gate
  --build-every=N      Run full 'npm run build' after every N migrations (default: 10; 0 disables)
  --no-final-build     Do not run the extra 'npm run build' at the end of the run
  --skip-build         Disable the periodic and final 'npm run build' entirely
  --build-fix-attempts=N  Agent attempts to fix a failing build (default: 2)
  --build-timeout=<d>  Timeout for each 'npm run build', zx duration (default: 20m)
  --dry-run            Build/show the queue and plan; do not call the agent
  --print-prompt       With --dry-run, print the rendered prompt for the next comp
  --status             Print progress summary from the saved queue and exit
  --rebuild-queue      Recompute the queue from disk (discards saved statuses)
  --max-failures=N     Abort after N consecutive failures (default: 3)
  --model=<id>         Model passed to kiro-cli (default: claude-opus-4.8)
  --effort=<level>     Reasoning effort: low|medium|high|xhigh|max (default: high)
  --skill=<name>       Skill the agent loads (default: angular-developer;
                       pass --no-skill to disable)
  --agent=<name>       kiro-cli agent to use (default: default agent)
  --agent-timeout=<d>  Per-component agent timeout, zx duration (default: 20m)
  -h, --help           Show this help

Gates (in order): npm run tsc-go -> npm run lint-biome -> the changed spec.
On any failure the component is reverted with 'git checkout --'. Never pushes.
After every --build-every migrations and at the end, a full 'npm run build' runs;
on failure the agent fixes it and rebuilds, else the run aborts.

Examples:
  npx zx tools/ralph/ralph.mjs --dry-run
  npx zx tools/ralph/ralph.mjs --limit=1 --commit
  npx zx tools/ralph/ralph.mjs --all --commit`;

if (argv.help || argv.h) {
  console.log(HELP);
  process.exit(0);
}

const opts = {
  limit: argv.all ? Infinity : argv.limit !== undefined ? Number(argv.limit) : 1,
  only: argv.only ? String(argv.only) : null,
  commit: Boolean(argv.commit ?? false),
  skipLint: Boolean(argv['skip-lint'] ?? false),
  skipTest: Boolean(argv['skip-test'] ?? false),
  skipBuild: Boolean(argv['skip-build'] ?? false),
  buildEvery: argv['build-every'] !== undefined ? Number(argv['build-every']) : 10,
  finalBuild: argv['final-build'] !== false,
  buildFixAttempts: argv['build-fix-attempts'] !== undefined ? Number(argv['build-fix-attempts']) : 2,
  buildTimeout: String(argv['build-timeout'] ?? '20m'),
  dryRun: Boolean(argv['dry-run'] ?? false),
  printPrompt: Boolean(argv['print-prompt'] ?? false),
  status: Boolean(argv.status ?? false),
  rebuildQueue: Boolean(argv['rebuild-queue'] ?? false),
  maxFailures: argv['max-failures'] !== undefined ? Number(argv['max-failures']) : 3,
  model: argv.model ? String(argv.model) : 'claude-opus-4.8',
  effort: argv.effort ? String(argv.effort) : 'high',
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

function featureOf(rel) {
  const parts = rel.split('/');
  return parts[2] || 'app';
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

function classify(src) {
  if (/ChangeDetectionStrategy\.OnPush/.test(src)) return { status: 'done', reason: 'already OnPush' };
  if (/ChangeDetectionStrategy\.Eager/.test(src)) return { status: 'pending', reason: 'on Eager (legacy Default)' };
  return { status: 'pending', reason: 'no explicit CD strategy (defaults to Eager)' };
}

// ---------------------------------------------------------------------------
// Queue discovery / persistence
// ---------------------------------------------------------------------------
async function discover() {
  const files = await glob('src/app/**/*.component.ts', {
    cwd: FRONTEND,
    ignore: ['**/*.spec.ts', 'src/app/core/generated/**'],
  });
  const items = [];
  for (const rel of files.sort()) {
    const src = await fs.readFile(path.join(FRONTEND, rel), 'utf8');
    const { status, reason } = classify(src);
    items.push({ file: rel, status, reason, attempts: 0, result: '' });
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
    if (/ChangeDetectionStrategy\.OnPush/.test(await fs.readFile(abs, 'utf8'))) {
      it.status = 'done';
      it.reason = 'already OnPush';
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
    '# Ralph loop report',
    '',
    `- Generated: ${new Date().toISOString()}`,
    `- Total components: ${t.total}`,
    `- Done (OnPush): ${t.done}`,
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
      `Angular 22 best practices. For this OnPush migration the most relevant`,
      `references (read as needed) are:`,
      `- \`references/components.md\` — component anatomy & @if/@for/@switch`,
      `- \`references/signals-overview.md\`, \`references/linked-signal.md\`, \`references/resource.md\`, \`references/effects.md\``,
      `- \`references/inputs.md\`, \`references/outputs.md\``,
      `- \`references/di-fundamentals.md\`, \`references/injection-context.md\``,
      `- \`references/pipes.md\``,
      `- \`references/testing-fundamentals.md\`, \`references/component-harnesses.md\` (only if you touch the spec)`,
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
  await fs.writeFile(
    logFile,
    `$ kiro-cli ${args.slice(0, -1).join(' ')} <prompt>\n\n${res.stdout ?? ''}\n----- STDERR -----\n${res.stderr ?? ''}`
  );
  return { ok: res.exitCode === 0, code: res.exitCode, stdout: res.stdout ?? '' };
}

// ---------------------------------------------------------------------------
// Verification gates: tsc-go -> lint-biome -> changed spec
// ---------------------------------------------------------------------------
async function verify(componentRel, logFile) {
  // 1) Fast whole-project type-check with the tsgo engine.
  let r = await run('npm run tsc-go');
  if (!r.ok) return { ok: false, gate: 'tsc-go', detail: tail(r.stdout + r.stderr) };

  // 2) Biome lint.
  if (!opts.skipLint) {
    r = await run('npm run lint-biome');
    if (!r.ok) return { ok: false, gate: 'lint-biome', detail: tail(r.stdout + r.stderr) };
  }

  // 3) The changed component's spec (also compiles its template via ng build).
  if (!opts.skipTest) {
    const spec = componentRel.replace(/\.ts$/, '.spec.ts');
    if (await exists(path.join(FRONTEND, spec))) {
      r = await run(`npm run test:file -- ${q(spec)}`, { timeout: '10m' });
      if (!r.ok) return { ok: false, gate: `test (${path.basename(spec)})`, detail: tail(r.stdout + r.stderr) };
    }
  }
  return { ok: true };
}

// ---------------------------------------------------------------------------
// Git (commit on success, revert on failure) — NEVER pushes
// ---------------------------------------------------------------------------
async function revert(componentRel) {
  const files = await existingRelated(componentRel);
  if (files.length) await run(`git checkout -- ${files.map(q).join(' ')}`);
}

async function commit(componentRel, summary) {
  const files = await existingRelated(componentRel);
  await run(`git add ${files.map(q).join(' ')}`);
  const msg =
    `refactor(${featureOf(componentRel)}): OnPush change detection for ` +
    `${path.basename(componentRel, '.ts')}\n\n${summary || 'Migrated to ChangeDetectionStrategy.OnPush.'}\n\n[ralph]`;
  const r = await run(`git commit -m ${q(msg)}`); // no push, ever
  return r.ok;
}

// ---------------------------------------------------------------------------
// Full production build checkpoint (npm run build) + agent-driven fix
// This is the only gate that runs Angular's whole-app AOT template
// type-checking, so it catches template<->TS signal-call mismatches that
// tsc-go / biome / a shallow spec can miss. NEVER pushes.
// ---------------------------------------------------------------------------
async function buildFixPrompt(errorTail) {
  return [
    `An automated "Ralph loop" has been migrating CircaBC Angular 22 components to`,
    `\`ChangeDetectionStrategy.OnPush\` one at a time. Each migrated component already`,
    `passed \`npm run tsc-go\`, \`npm run lint-biome\`, and its own spec, but the full`,
    `production build \`npm run build\` (\`ng build\`, which runs Angular's whole-app`,
    `AOT template type-checking) now FAILS.`,
    ``,
    skillBlock(),
    `## Your task`,
    `Diagnose and fix the build errors shown below with the SMALLEST possible change.`,
    `They are almost always a side effect of the OnPush migrations, most commonly:`,
    `- a field converted to a signal in the \`.ts\` but still read without \`()\` in its`,
    `  \`.html\` (or the reverse) — e.g. \`{{ foo }}\` must become \`{{ foo() }}\`;`,
    `- a signal passed where the call form is required in a binding;`,
    `- a missing \`ChangeDetectionStrategy\` import after adding \`changeDetection\`;`,
    `- an \`@if\` / \`@for\` conversion missing a \`track\` expression.`,
    ``,
    `## Constraints`,
    `- Make minimal, targeted edits — fix only what the build reports; do NOT reformat`,
    `  untouched lines or refactor unrelated code.`,
    `- Do NOT change any component's public API (selector, input/output names and`,
    `  types, exported symbols) and do NOT revert an \`OnPush\` change. The goal is to`,
    `  KEEP the OnPush migrations and make the build green by fixing the source of each`,
    `  error (e.g. update the template to call a signal; do not downgrade the signal`,
    `  back to a plain field).`,
    `- Do NOT run \`npm run build\`, \`ng build\`, \`ng test\`, or ANY git command`,
    `  yourself. The orchestrator re-runs \`npm run build\` to verify your fix and owns`,
    `  version control.`,
    ``,
    `## Build output (tail)`,
    '```',
    errorTail,
    '```',
    ``,
    `End your response with exactly one line prefixed literally with \`RALPH_RESULT:\``,
    `summarizing what you changed (e.g.`,
    `\`RALPH_RESULT: fixed 2 template signal-call sites in news-card and admin\`).`,
  ].join('\n');
}

async function commitBuildFix(label) {
  await run('git add -A -- src');
  const msg = `fix(build): resolve build errors after ralph batch (${label})\n\n[ralph]`;
  const r = await run(`git commit -m ${q(msg)}`); // no push, ever
  return r.ok;
}

/**
 * Run `npm run build`; on failure ask the agent to fix it (up to
 * opts.buildFixAttempts) and rebuild. Returns {ok, detail?}.
 */
async function buildCheckpoint(label) {
  process.stdout.write(`  build (${label})… `);
  await fs.mkdir(LOG_DIR, { recursive: true });
  const logFile = path.join(LOG_DIR, `build-${label}.log`);

  let r = await run('npm run build', { timeout: opts.buildTimeout });
  await fs.writeFile(logFile, `$ npm run build\n\n${r.stdout}\n----- STDERR -----\n${r.stderr}`);
  if (r.ok) {
    console.log(chalk.green('passed'));
    return { ok: true };
  }
  console.log(chalk.yellow('failed — attempting fix'));

  for (let attempt = 1; attempt <= opts.buildFixAttempts; attempt++) {
    process.stdout.write(`    fix ${attempt}/${opts.buildFixAttempts}… `);
    const fixLog = path.join(LOG_DIR, `build-${label}-fix${attempt}.log`);
    const agent = await runAgent(await buildFixPrompt(tail(r.stdout + r.stderr, 200)), fixLog);
    const resultLine = parseResult(agent.stdout);
    r = await run('npm run build', { timeout: opts.buildTimeout });
    await fs.appendFile(
      logFile,
      `\n----- BUILD AFTER FIX ${attempt} (agent exit ${agent.code}) -----\n${resultLine}\n${tail(r.stdout + r.stderr)}\n`
    );
    if (r.ok) {
      console.log(chalk.green('fixed'));
      if (resultLine) console.log('      ' + chalk.gray(resultLine));
      if (opts.commit) {
        const ok = await commitBuildFix(label);
        console.log('    commit… ' + (ok ? chalk.green('done') : chalk.yellow('nothing to commit')));
      }
      return { ok: true, fixed: true };
    }
    console.log(chalk.red('still failing'));
  }
  return { ok: false, detail: tail(r.stdout + r.stderr) };
}

async function abortOnBuild(queue, detail) {
  await saveQueue(queue);
  await writeReport(queue);
  console.error(
    chalk.red(`\nAborted: 'npm run build' still fails after ${opts.buildFixAttempts} fix attempt(s).`)
  );
  console.error(
    chalk.gray(
      `Migrated components are preserved (committed if --commit); any uncommitted changes are the\n` +
        `in-progress build fix. Inspect the build log under ${path.relative(FRONTEND, LOG_DIR)}/, fix\n` +
        `manually, then re-run the same command to resume.`
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
        `(gates: tsc-go${opts.skipLint ? '' : ' -> lint-biome'}${opts.skipTest ? '' : ' -> spec'}; commit: ${opts.commit})`
      )
  );
  console.log(
    chalk.gray(`      agent: kiro-cli • model: ${opts.model} • effort: ${opts.effort} • skill: ${opts.skill ?? 'none'}`)
  );
  if (opts.skipBuild) {
    console.log(chalk.gray('      build: disabled (--skip-build)'));
  } else {
    const cadence = opts.buildEvery > 0 ? `every ${opts.buildEvery} migration(s)` : 'periodic off';
    console.log(
      chalk.gray(
        `      build: npm run build ${cadence}${opts.finalBuild ? ' + at end' : ''} • fix attempts: ${opts.buildFixAttempts}`
      )
    );
  }

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

    // 1) Agent improves just this component.
    process.stdout.write('  agent… ');
    const agent = await runAgent(await buildPrompt(item.file), logFile);
    const resultLine = parseResult(agent.stdout);
    console.log(agent.ok ? chalk.green('done') : chalk.red(`exit ${agent.code}`));
    if (resultLine) console.log('  ' + chalk.gray(resultLine));

    if (/OnPush blocked/i.test(resultLine)) {
      // Agent left a TODO note and did not migrate; keep the annotation, don't commit.
      item.status = 'blocked';
      item.result = resultLine;
      consecutiveFailures = 0;
      await saveQueue(queue);
      continue;
    }
    if (!agent.ok) {
      await revert(item.file);
      item.status = 'failed';
      item.result = `agent exited ${agent.code}; see ${path.relative(FRONTEND, logFile)}`;
      consecutiveFailures += 1;
      await saveQueue(queue);
      if (consecutiveFailures >= opts.maxFailures) return abort(queue, consecutiveFailures);
      continue;
    }

    // 2) Verify: tsc-go -> lint-biome -> spec.
    process.stdout.write('  verify… ');
    const v = await verify(item.file, logFile);
    if (!v.ok) {
      console.log(chalk.red(`FAILED at ${v.gate}`));
      await fs.appendFile(logFile, `\n----- VERIFY FAILED (${v.gate}) -----\n${v.detail}\n`);
      await revert(item.file);
      item.status = 'failed';
      item.result = `${v.gate} failed (reverted); see ${path.relative(FRONTEND, logFile)}`;
      consecutiveFailures += 1;
      await saveQueue(queue);
      if (consecutiveFailures >= opts.maxFailures) return abort(queue, consecutiveFailures);
      continue;
    }
    console.log(chalk.green('passed'));

    // 3) Commit (opt-in). Never pushes.
    if (opts.commit) {
      const ok = await commit(item.file, resultLine);
      console.log('  commit… ' + (ok ? chalk.green('done') : chalk.yellow('nothing to commit')));
    }

    item.status = 'done';
    item.result = resultLine || 'migrated to OnPush';
    consecutiveFailures = 0;
    doneThisRun += 1;
    sinceLastBuild += 1;
    await saveQueue(queue);

    // Periodic full-build checkpoint after every N successful migrations.
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

async function abort(queue, failures) {
  await saveQueue(queue);
  await writeReport(queue);
  console.error(chalk.red(`\nAborted after ${failures} consecutive failures. Fix the cause and re-run to resume.`));
  process.exit(1);
}

await main();
