# Advanced Compilation

This document explains the different TypeScript type-checking / compilation
paths available in the frontend project, and how the experimental
`strictArity` option is configured.

## Overview of compilers

The project can be type-checked with three different compilers. All three are
type-check / build passes only — the production bundle is still produced by
`npm run build` (the Angular `@angular/build:application` builder).

| npm script | Compiler | Project config | Notes |
|---|---|---|---|
| `npm run tsc` | Official `tsc` (TypeScript, JS implementation) | `src/tsconfig.app.json` | Canonical type-check. Uses the Angular compiler options and template checking via the extended config chain. |
| `npm run tsc-go` | `@typescript/native-preview` (`tsgo`, official Go port) | `src/tsconfig.tsgo.json` | ~10x faster native type-check preview from Microsoft. |
| `npm run topce-tsc-go` | `@topce/native-preview` (`tsgo`, community fork) | `src/tsconfig.topce.json` | Same native port plus the extra experimental `strictArity` diagnostic (not present in upstream). |

```bash
npm run tsc            # official tsc  (src/tsconfig.app.json)
npm run tsc-go         # official tsgo (src/tsconfig.tsgo.json)
npm run topce-tsc-go   # topce  tsgo  (src/tsconfig.topce.json)  -> adds strictArity
```

> The three configs are independent. `tsconfig.app.json` extends the root
> `tsconfig.json` and carries the Angular compiler options; `tsconfig.tsgo.json`
> and `tsconfig.topce.json` are standalone configs tuned for the native port.

## The `strictArity` option (topce fork only)

`strictArity` is an experimental compiler option added by the
`@topce/native-preview` fork. It is **not** part of upstream TypeScript or the
official native preview, so it only has an effect via `npm run topce-tsc-go`.

### What it does

It applies **strict parameter-count (arity) checking** when comparing
signatures. Standard TypeScript intentionally lets a callback declare *fewer*
parameters than the signature it is assigned to (this is why
`arr.map(x => ...)` is legal even though `map` passes `value, index, array`).
`strictArity` re-enables an error when the target signature provides more
arguments than the callback declares, for the selected kinds of declarations.

### Value format

The value is parsed by the fork's `ParseStrictArity`. It accepts:

- a single token, e.g. `"arrowfunction"`
- a comma-separated list, e.g. `"callsignature,arrowfunction"`
- the presets `"all"` or `"none"`
- the booleans `"true"` (= all) / `"false"` (= none)
- a raw numeric bitmap

Matching is **case-insensitive**. **Unknown tokens are silently ignored**
(so a typo such as `"call"` behaves like `"none"` — no error, no checking).

### Accepted kind tokens

| Token | Declaration kind it checks |
|---|---|
| `callsignature` | interface/type call signature: `(x: number): void` |
| `constructsignature` | construct signature: `new (): Foo` |
| `methodsignature` | interface method signature: `foo(): void` |
| `methoddeclaration` | class method: `foo() { }` |
| `constructor` | class constructor: `constructor() { }` |
| `functiondeclaration` | `function foo() { }` |
| `functionexpression` | `const x = function () { }` |
| `arrowfunction` | `() => { }` |
| `functiontype` | function type annotation: `(x: number) => void` |
| `constructortype` | constructor type annotation: `new (x: number) => Foo` |
| `all` | all of the above |
| `none` | disabled (zero) |

## Configuration used by this project

`strictArity: "all"` does **not** pass on the current codebase. Two kinds
account for every failure:

- `functiontype` — ~342 errors (e.g. `CanActivateFn`, RxJS operator/callback
  types, `XMLHttpRequest.onreadystatechange`).
- `methodsignature` — ~13 errors (e.g. lib `Array.find/filter/map/forEach/some`,
  `Promise.catch`, `Map.forEach`).

Every other kind checks clean. To get the **maximum** strict-arity coverage
that still compiles, `src/tsconfig.topce.json` enables all kinds **except**
`functiontype` and `methodsignature`:

```jsonc
{
  "compilerOptions": {
    // ...
    "strictArity": "callsignature,constructsignature,methoddeclaration,constructor,functiondeclaration,functionexpression,arrowfunction,constructortype"
  }
}
```

### Per-kind status (measured)

| Token | Errors | Passes |
|---|---:|:---:|
| `callsignature` | 0 | ✅ |
| `constructsignature` | 0 | ✅ |
| `methoddeclaration` | 0 | ✅ |
| `constructor` | 0 | ✅ |
| `functiondeclaration` | 0 | ✅ |
| `functionexpression` | 0 | ✅ |
| `arrowfunction` | 0 | ✅ |
| `constructortype` | 0 | ✅ |
| `methodsignature` | 13 | ❌ |
| `functiontype` | 342 | ❌ |
| `all` | 355 | ❌ |

## Reproducing / measuring

Error counting must disable pretty output, because ANSI color codes are
inserted between `error` and the `TS####` code, which breaks naive
`grep "error TS"`:

```bash
# count errors for the current strictArity value
node node_modules/@topce/native-preview/bin/tsgo \
  --project src/tsconfig.topce.json --pretty false 2>&1 | grep -c "error TS"
```

To try a different value, edit `strictArity` in `src/tsconfig.topce.json` and
re-run. Note: passing `--strictArity` on the command line does **not** override
the value in the project config, so change the config file.

## Roadmap notes

- `functiontype` and `methodsignature` are the two kinds to tackle if full
  `strictArity: "all"` coverage is desired. Many failures originate in the
  auto-generated API clients under `src/app/core/generated/` (which must not be
  hand-edited) and in library `.d.ts` signatures, so full coverage would
  require upstream/codegen changes rather than local edits.
- `strictArity` is experimental and fork-specific; keep it on the
  `topce-tsc-go` path only and do not rely on it for the official
  `npm run tsc` / `npm run build` gates.
