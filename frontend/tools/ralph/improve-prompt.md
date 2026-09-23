You are improving EXACTLY ONE Angular 22 standalone component in the CircaBC
frontend. This is one iteration of an automated "Ralph loop": an orchestrator
picks a component, you improve just that component, then the orchestrator runs
`npm run tsc-go`, `npm run lint-biome`, and this component's spec, and commits
the result if all gates pass. Keep your work tightly scoped and your diff
minimal — change only what the migration requires, never reformat untouched
lines.

{{SKILL_BLOCK}}
## Target
- Component: `{{COMPONENT_PATH}}`
- Editable related files (only if needed): {{RELATED_FILES}}

## Primary objective — enable OnPush change detection
Migrate this component's change detection to `ChangeDetectionStrategy.OnPush`.
In this repo the enum value `ChangeDetectionStrategy.Eager` is the name for the
legacy `Default` strategy, so almost every component reads:

```ts
@Component({
  // ...
  changeDetection: ChangeDetectionStrategy.Eager,
})
```

and must become:

```ts
@Component({
  // ...
  changeDetection: ChangeDetectionStrategy.OnPush,
})
```

If the component has no explicit `changeDetection` at all, it is also on the
default strategy — add `changeDetection: ChangeDetectionStrategy.OnPush` (and
import `ChangeDetectionStrategy` from `@angular/core` if it isn't already).

### Why OnPush safety matters here
This app runs with **zone.js change detection (it is NOT zoneless)**. Under
`Eager`/Default, every async task re-renders the view, so components get away
with writing plain (non-signal) fields from async callbacks. Under **OnPush**,
the view only re-renders when Angular marks it dirty — which happens for:
signal reads that change, template `(event)` handlers, `@Input()`/`input()`
changes, and the `async` pipe. It does **NOT** happen automatically when you
write a plain field from an async callback such as `setTimeout`, a `Promise`
`.then`/`await` continuation, an RxJS `subscribe()`, a Material dialog
`afterClosed().subscribe()`, or a third-party callback (Quill, mgt, pdf
viewer). Those are the migrations that silently break the UI, so find and fix
them before flipping the strategy.

### OnPush-safety checklist — walk this before flipping the flag
1. **Inventory template-bound state.** List every field/getter the `.html`
   reads (interpolation, bindings, `@if`/`@for` conditions, `[class]`, etc.).
2. **For each, is it already reactive?** Signals (`signal`, `computed`,
   `input()`, `model()`, `linkedSignal`, `resource`, `toSignal`) and the
   `async` pipe are OnPush-safe. Leave them as-is.
3. **Find plain fields written from async callbacks.** Any plain field that is
   assigned inside `setTimeout`/`setInterval`, a `Promise`/`await`
   continuation, `.subscribe(...)`, `afterClosed()`, or a third-party callback
   is unsafe under OnPush. Convert it to a `signal` (or `toSignal`/`async`
   pipe) and update every read: `this.show` → `this.show()` in TS and `show` →
   `show()` in the template.
4. **Find in-place mutation of template-bound arrays/objects.** Replace
   `arr.push(x)` / `obj.prop = y` on template-bound structures with immutable
   reassignment via a signal `set`/`update` (e.g.
   `this.items.set([...this.items(), x])`, spread, `map`, `filter`) so OnPush
   observes a new reference.
5. **Prefer signals over `markForCheck()`.** Only fall back to an injected
   `ChangeDetectorRef` + `markForCheck()` when a signal/`async` conversion is
   genuinely impractical (e.g. imperative third-party integration), and explain
   why in a short code comment.
6. **Flip to `OnPush`** once all template-bound state is reactive.

### Concrete example (this hazard is real and common in this repo)
```ts
// BEFORE — plain field mutated from setTimeout; safe only under Eager
public show = false;
showIt() {
  this.show = true;
  setTimeout(() => { this.show = false; this.snackFinished.emit(); }, this.duration());
}
// template: <div [ngClass]="{ show: show }">

// AFTER — signal-backed, OnPush-safe
public readonly show = signal(false);
showIt() {
  this.show.set(true);
  setTimeout(() => { this.show.set(false); this.snackFinished.emit(); }, this.duration());
}
// template: <div [ngClass]="{ show: show() }">
```

### Patterns that are ALREADY OnPush-safe — do NOT rewrite them
Avoid unnecessary refactors; they enlarge the diff and break the gates.
- Template `(click)`/`(input)`/etc. handlers that assign plain fields — the
  event itself marks the view dirty, so these are fine under OnPush.
- Fields written only from `ngOnChanges` or an `@Input() set` — an input change
  already marks the view; no signal conversion required for safety.
- Existing `computed`, `input()`, `output()`, `model()`, `toSignal`, and
  `async` pipe usage.
- Existing `ChangeDetectorRef.markForCheck()`/`detectChanges()` calls that are
  already correct — leave them unless a clean signal conversion removes the
  need.

## Secondary objectives — only when trivially safe and inside this component
Do these only if they are obviously safe and will not risk the gates:
- Prefer `inject()` over constructor parameter injection.
- Use built-in control flow (`@if` / `@for` / `@switch`) if any legacy
  `*ngIf` / `*ngFor` / `*ngSwitch` remain. Every `@for` needs a `track`.
- Convert `@Input()` / `@Output()` decorators to `input()` / `output()`.
- Convert `@ViewChild` / `@ContentChild` to the `viewChild()` / `contentChild()`
  signal queries.
- Remove dead code and tighten types (avoid `any`; prefer `unknown` + narrowing).

## Hard constraints
- Touch ONLY the target component's own files (the ones listed above). Do NOT
  edit shared services, other components, routing modules, or generated code.
- Preserve the public API exactly: selector, `input`/`output` names and types,
  exported symbols, `preserveWhitespaces` and other metadata, and observable
  template behavior must not change.
- Preserve existing JSDoc/TSDoc comments and match the file's existing code
  style and formatting (this repo uses Prettier with single quotes).
- Do NOT run `ng build`, `ng test`, or ANY git command yourself. The
  orchestrator owns verification and version control: after you finish it runs
  `npm run tsc-go`, `npm run lint-biome`, and this component's spec, then commits
  (or reverts on failure). Reason carefully about types and templates instead of
  launching long-running processes. Note `tsc-go` type-checks the WHOLE project,
  so any type error you introduce anywhere will fail the gate.
- If you change fields the template reads (e.g. field -> signal), update the
  component's `.html` so template bindings stay correct (`foo` -> `foo()`), and
  update the `.spec.ts` if the change would otherwise break it (e.g. a test that
  reads or sets `component.foo` must use `component.foo()` / `component.foo.set(...)`).
- If the component CANNOT be safely migrated to OnPush, leave `changeDetection`
  unchanged, add a single-line `// TODO(ralph): OnPush blocked — <short reason>`
  above the `@Component` decorator, and do not force it.

## Required output
End your response with exactly one line, prefixed literally with `RALPH_RESULT:`,
summarizing the outcome. Use one of these forms:
- On success, the line MUST start with `migrated to OnPush`:
  - `RALPH_RESULT: migrated to OnPush`
  - `RALPH_RESULT: migrated to OnPush; converted 1 subscribe to toSignal`
  - `RALPH_RESULT: migrated to OnPush; show field -> signal for setTimeout callback`
- If you could not migrate, the line MUST contain the literal phrase
  `OnPush blocked` (the orchestrator keys on it to skip the commit — any other
  wording will be mistaken for success):
  - `RALPH_RESULT: OnPush blocked — mutates \`rows\` array in place from a callback`
