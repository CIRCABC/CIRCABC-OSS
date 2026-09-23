You are migrating exactly ONE Angular 22 component in the CircaBC frontend from
imperative `async ngOnInit()` data loading to reactive `resource()` + signals.

Work ONLY on this component and its co-located files:

- Component: `{{COMPONENT_PATH}}`
- Related files you may edit: {{RELATED_FILES}}

Do NOT touch any other component, shared service, or the generated API clients
under `src/app/core/generated/**`. Do NOT run `ng build`, `ng test`, or any git
command — the orchestrator handles verification and version control.

{{SKILL_BLOCK}}
## Project facts

- Angular is **22.1.1** — `resource()`, `computed()`, `linkedSignal()`, signal
  `input()`/`output()` are all available and preferred.
- The generated API clients expose **promise-based** `...Async(params)` methods
  (e.g. `getFooAsync({ id })`). USE THESE inside the resource loader. The user
  has explicitly asked NOT to use the generated `...Resource` (rxResource)
  helpers — wrap the `...Async` promise methods in `resource()` instead.
- Components use `ChangeDetectionStrategy.OnPush` and standalone imports.

## Choosing the right signal primitive (Angular v22)

Pick the narrowest primitive that fits; reach for `effect` last.

- **`resource()`** — async data loaded from a source (the target of this
  migration). Wrap the promise-based `...Async` service method in the `loader`.
  <https://angular.dev/guide/signals/resource>
- **`computed()`** — a value SYNCHRONOUSLY derived from other signals (including
  a resource's `value()`), e.g. filtering/de-duping a loaded list, or deriving
  `restCallError` from `status()`. Never copy one signal into another by hand.
  <https://angular.dev/guide/signals>
- **`linkedSignal()`** — state that is DERIVED from a source but must also be
  locally writable. Use it when the old `ngOnInit` loaded data and then seeded a
  writable field the user can later change (e.g. "default the selection to the
  first loaded option, but keep the user's choice"). Prefer it over an `effect`
  that copies loaded data into a `signal`. Use the `{ source, computation }`
  form with `previous` to preserve a still-valid selection.
  <https://angular.dev/guide/signals/linked-signal>
- **`debounced()`** (experimental) — when a resource's `params` come from a
  fast-changing input like a search box, debounce the source signal so the
  loader doesn't fire on every keystroke:
  `readonly q = signal(''); private readonly dq = debounced(this.q, 300);`
  then `params: () => this.dq.value()`. Only introduce this if the old code had
  search-as-you-type/manual debouncing; otherwise keep params direct.
  <https://angular.dev/guide/signals/debounced>
- **`effect()`** — LAST RESORT, only for syncing signals to non-signal
  imperative APIs (localStorage, logging, 3rd-party/DOM libs). Do NOT use an
  effect to propagate/copy state between signals (causes
  `ExpressionChangedAfterItHasBeenChecked`, loops, extra CD). For DOM work after
  render use `afterRenderEffect`. If the old `ngOnInit` did such a genuine side
  effect (not data loading), see "When NOT to migrate" below.
  <https://angular.dev/guide/signals/effect>

## The migration

Replace the `async ngOnInit()` (and any private `loadX()`/`getX()` helper it
calls purely to fetch data) with one or more `resource()` fields, and expose the
loaded data through signals.

### 1. Pick the resource shape

- **No parameters** (loads once): a loader with no `params`.
  ```ts
  private readonly fooResource = resource({
    loader: () => this.fooService.getFooAsync({ amount: 10 }),
  });
  ```
- **Parameterised / reactive** (depends on an `input()`, route param, or another
  signal): use `params` so it re-runs when the source changes. Returning
  `undefined` from `params` keeps the resource IDLE (loader not called) — use
  this to reproduce guards like `if (id && id !== '')` or "only when logged in".
  ```ts
  private readonly fooResource = resource({
    params: () => this.id() || undefined,          // idle while empty
    loader: ({ params: id }) => this.fooService.getFooAsync({ id }),
  });
  ```
- Compute derived request values (date ranges, etc.) INSIDE the loader.
- The loader receives `{ params, previous, abortSignal }`. If a call is
  cancellable (e.g. it ultimately reaches `fetch`/`HttpClient`), forward
  `abortSignal` so an in-flight request is aborted when `params` change. The
  generated `...Async` methods generally don't accept it — skip it then.
- **Dependent (sequential) loads:** two options —
  - do both awaits in ONE loader and return a combined object:
    `return { members: a.count, applicants: b.length };` (simplest; use when the
    second call always follows the first), or
  - use separate resources with `chain()` in `params` when the second resource
    should mirror the first's loading/error states:
    `params: ({ chain }) => chain(userResource)?.companyId`. Prefer `chain` over
    reading `upstream.value()` in `params` (which would go `idle` instead of
    reflecting loading/error). For a purely synchronous derivation use
    `computed`, not `chain`.
- **Manual refresh:** if the old code re-called its load method (e.g. a
  "refresh" button, or after a create/delete mutation), expose that by calling
  `this.fooResource.reload()` instead of re-running an imperative fetch.

### 2. Expose signals (match the OLD public API names exactly)

- Data: assign `.value` directly when the old field was a plain data signal:
  `public readonly foo = this.fooResource.value;` (add `defaultValue: []`/`{…}`
  in the resource options so it is never `undefined` when the template iterates).
- Loading flag: `public readonly loading = this.fooResource.isLoading;`
- Error flag: `public readonly restCallError = computed(() => this.fooResource.status() === 'error');`
- Derived/transformed data (e.g. de-dup, filter): use `computed(...)` reading the
  resource value.

### 3. CRITICAL — `resource.value()` THROWS in the error state

Per the docs, `value()` is `undefined` in the `idle`/`loading`/`error` states,
and reading it while the resource is in the ERROR state THROWS. `hasValue()`
serves two purposes: it narrows the type (strips `undefined`) AND protects
against reading a throwing `value()`. So if the template (or a `computed`) reads
`resource.value()` while it may be errored, it breaks rendering. Choose per
component:

- **No dedicated error UI** (the old code just showed an empty list / logged):
  catch inside the loader and return a safe default, so the resource never ends
  in the error state:
  ```ts
  loader: async ({ params: id }) => {
    try { return await this.fooService.getFooAsync({ id }); }
    catch (e) { console.error(e); return []; }
  },
  ```
- **Has a dedicated error UI** (template shows an error section when a flag is
  true): keep the error state (do NOT catch), derive `error` from
  `status() === 'error'`, and read the value through a guard so it never throws:
  ```ts
  public readonly foo = computed(() =>
    this.fooResource.hasValue() ? this.fooResource.value() : undefined // or []
  );
  ```
- Preserve any error side effects the old `catch` performed (e.g.
  `uiMessageService.addErrorMessage(...)`, parsing `error._body`) inside the
  loader's `try/catch`.

### 4. Pagination / reload

- Replace mutable `listingOptions.page` + `loadX()` with a `page = signal(1)`
  read in the resource `params`. A `changePage(p)` handler becomes
  `this.page.set(p)` (drop `async`/`await`) — the resource reloads reactively.
- Update the template bindings accordingly (e.g. `[page]="page()"`,
  `[pageSize]="limit"`), and derived getters like `isPagerVisible()` to read the
  signals.

### 5. Cleanup

- Remove `implements OnInit`, the `ngOnInit` method, the now-unused private
  fetch helper, and drop unused imports (`OnInit`, `signal` if no longer used,
  types only referenced by removed code). Add `computed`/`resource` imports.
- Keep JSDoc updated and accurate. Keep the class's public field/method names
  the same so the template and specs keep working (adjust only what changed,
  e.g. a getter that referenced `listingOptions`).

### 6. Template (`.html`)

- Signals are called: `foo()`, `loading()`, `restCallError()`.
- If you turned a writable field into a signal that the template previously read
  without `()`, add the `()`.

### 7. Spec (`.spec.ts`) — keep it green

The orchestrator runs this component's spec. Update it to the resource model:

- Resources load in a reactive effect, so drive them with change detection
  instead of calling `ngOnInit()`:
  ```ts
  fixture.detectChanges();
  await fixture.whenStable();
  ```
  Replace every `await component.ngOnInit()` with the two lines above (keep a
  reference to the `fixture`; add `ComponentFixture` to the import if needed).
- Do NOT call removed methods (`loadX`, `getX`) or set now-`computed`/read-only
  signals (e.g. `component.total.set(...)`). Instead make the mock service
  resolve the value you need and assert after `whenStable()`.
- If loading data makes the template render a `routerLink`/`ActivatedRoute`
  consumer that wasn't exercised before, add `provideRouter([])` to the
  TestBed `providers` (import from `@angular/router`). This is a common failure
  when the old spec called `ngOnInit()` directly without rendering.
- Keep the existing test intent/assertions; only adapt the mechanism.

## When NOT to migrate (report blocked)

If the `async ngOnInit` does NOT fetch data into state — e.g. it only sets up a
reactive form, subscribes to something, does imperative DOM/router side effects,
or awaits an action with no returned state to store — then `resource()` is the
wrong tool. In that case make NO changes and end your response with:

`RALPH_RESULT: RALPH_BLOCKED — <one-line reason>`

## Definition of done

- No `async ngOnInit` remains in `{{COMPONENT_PATH}}` (the method is gone, not
  just renamed).
- Data is loaded via `resource()` wrapping the `...Async` service method(s).
- The component, template, and spec are internally consistent.

End your response with EXACTLY one line prefixed literally with `RALPH_RESULT:`
summarizing what you did, e.g.:
`RALPH_RESULT: converted events load to resource(params=igId); events/loading/restCallError as computed; spec uses whenStable + provideRouter`
