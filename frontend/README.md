# CircaBC Frontend

Angular 22 single-page application for CircaBC, built with the Angular CLI
(`@angular/build`), TypeScript and Angular Material. Served in production behind
Nginx.

## Prerequisites

Tool versions are managed by [mise](https://mise.jdx.dev/) via `mise.toml`
(Node 24). Install them once:

```bash
mise install     # Node 24
npm install      # or: npm ci  (clean, lockfile-exact install)
```

## Development server

```bash
npm start        # ng serve -o  → http://localhost:4200
npm run next     # ng serve -c=next -o  (recommended dev configuration)
```

API calls are proxied to the backend (`:8080`) and EU Captcha (`:9898`) — see
the `src/proxy.conf*.json` files. Keep the backend running in another terminal.

## Build

```bash
npm run build    # production build into dist/circabc
```

## Type-checking, linting & formatting

| Script | Purpose |
|--------|---------|
| `npm run tsc` | TypeScript type-check (`src/tsconfig.app.json`) |
| `npm run tsc-go` | Faster native type-check preview (`tsgo`) |
| `npm run lint` | ESLint (Angular) |
| `npm run lint-biome` | Biome lint |
| `npm run stylelint` | SCSS lint |
| `npm run format-check` | Prettier check (HTML + TS) |
| `npm run bamboo` | Full local CI gate: build-date → tsc → format → lint → stylelint → build |

See [`docs/advanced-compilation.md`](./docs/advanced-compilation.md) for the
alternative compilers (`tsc-go`, `topce-tsc-go`) and the experimental
`strictArity` option.

## Unit tests (Vitest)

```bash
npm test                 # run unit tests (Vitest)
npm run test:nw          # single run, no watch (CI-style)
npm run test:coverage    # with coverage report
npm run test:watch       # watch mode during development
npm run test:file -- src/app/path/to/foo.spec.ts   # a single spec file
```

## End-to-end tests

E2E tests live in the top-level [`e2e/`](../e2e) project and run with
Playwright. See [`e2e/README.md`](../e2e/README.md).

## API client generation

The typed API clients under `src/app/core/generated/` are generated from the
OpenAPI specs in `apis/` via [openapi-generator](https://openapi-generator.tech/):

```bash
npm run circabc-api        # regenerate from apis/openapi.yaml
npm run ares-bridge-api    # regenerate from apis/ares-bridge.yaml
```

Do not hand-edit the generated files.

## Internationalization

Translation catalogs live in `src/assets/i18n/*.json`. Helper scripts for
keeping them flat, sorted and in sync are documented in
[`tools/README.md`](./tools/README.md).

## Runtime configuration (Docker)

The production image loads its configuration at container startup from
`CIRCABC_*` environment variables (no rebuild per environment). See
[`frontend-env.md`](./frontend-env.md) for the full mapping and mechanism.

## Further help

- Repo overview: [`../README.md`](../README.md)
- Developer onboarding: [`../onboarding.md`](../onboarding.md)
- Angular CLI: `ng help` or <https://angular.dev>
