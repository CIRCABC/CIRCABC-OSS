# CIRCABC

**Communication and Information Resource Centre for Administrations, Businesses and Citizens**

CIRCABC is an open-source collaborative workspace that lets groups create, share,
and manage documents and information across organisational boundaries. It is used
to host **Interest Groups** in which members collaborate through document
libraries, events, newsgroups/forums, and information pages, with fine-grained
permissions and multilingual content.

This repository is the public, open-source distribution of CIRCABC. It is a
sanitized mirror of the maintainers' development repository and excludes internal
deployment configuration and infrastructure tooling.

---

## Table of contents

- [What is CIRCABC?](#what-is-circabc)
- [Architecture](#architecture)
- [Project structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Quick start with Docker](#quick-start-with-docker)
- [Default URLs](#default-urls)
- [Default credentials](#default-credentials)
- [EU Login mock](#eu-login-mock)
- [Running services individually](#running-services-individually)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

---

## What is CIRCABC?

CIRCABC provides:

- **Interest Groups** — self-contained collaboration spaces with their own members
  and permission model.
- **Document libraries** — versioned document storage with check-in/out, and
  workflow.
- **Events, newsgroups and forums** — group communication tools.
- **Information pages** — publishable rich content per group.
- **Fine-grained authorization** — category/group/service-level roles.
- **Multilingual content and UI**.

The backend is built on **Alfresco Content Services**; the UI is a modern
**Angular** single-page application; a small **EU Captcha** service provides
CAPTCHA support.

## Architecture

```
                         ┌───────────────────────────────────────────────┐
   Browser ── :4200 ────▶│  frontend (Angular SPA, served at /ui)         │
                         └───────────────┬───────────────────────────────┘
                                         │  REST (proxied)
                                         ▼
                         ┌───────────────────────────────────────────────┐
                :8080 ──▶│  backend  (Alfresco Content Services +         │
                         │           CIRCABC REST API, Java 21)           │
                         └───────┬───────────────┬───────────────┬───────┘
                                 │               │               │
                                 ▼               ▼               ▼
                         ┌──────────────┐ ┌────────────┐ ┌────────────────┐
                         │ PostgreSQL   │ │ Solr        │ │ eu-captcha     │
                         │ (database)   │ │ (search)    │ │ (CAPTCHA svc)  │
                         └──────────────┘ └────────────┘ └────────────────┘

   Authentication (optional, local dev):
   Browser / backend ── :7002 ─▶ eulogin-mock  (mock EU Login / CAS OAuth2 provider)
```

| Layer     | Technology                                                        |
|-----------|-------------------------------------------------------------------|
| Frontend  | Angular · TypeScript · Angular Material (served under `/ui`)       |
| Backend   | Java 21 · Alfresco SDK · Spring WebScripts · REST API              |
| Database  | PostgreSQL                                                        |
| Search    | Alfresco Search Services (Solr)                                   |
| CAPTCHA   | Java 21 · Spring Boot (`eu-captcha`)                              |
| Auth      | EU Login / CAS (a mock provider is bundled for local development) |

## Project structure

```
circabc/
├── backend/       Java 21 · Maven · Alfresco SDK · CIRCABC REST API
├── frontend/      Angular · TypeScript · Angular Material
├── eu-captcha/    Java 21 · Maven · Spring Boot · CAPTCHA service
├── e2e/           Playwright end-to-end tests + axe-core accessibility audits
├── docker-test/   Self-contained local test environment + EU Login mock
├── CONTRIBUTING.md
├── README.md
└── LICENSE        EUPL-1.2
```

## Prerequisites

| Tool            | Version    | Used by              |
|-----------------|------------|----------------------|
| Docker          | 24+ (with Compose plugin) | the Docker test env |
| Java            | temurin-21 | backend, eu-captcha  |
| Maven           | 3.9+       | backend, eu-captcha  |
| Node.js         | 24.x       | frontend, e2e        |

Tool versions are managed by [mise](https://mise.jdx.dev/); each sub-project has a
`mise.toml`. Run `mise install` in a sub-project to get the pinned versions.

## Quick start with Docker

The fastest way to try CIRCABC — backend, database, search, frontend, and a mock
EU Login provider — is the bundled Docker test environment.

```bash
# 1. Build the backend image once (packages the Alfresco WAR).
cd backend && ./run.sh build_start && cd ..

# 2. Start the full stack.
docker compose -f docker-test/docker-compose.yml up -d

# 3. Wait ~1–2 minutes for Alfresco to finish starting, then open the UI:
#    http://localhost:4200/ui
```

Stop and clean up (removes data volumes):

```bash
docker compose -f docker-test/docker-compose.yml down -v
```

See [`docker-test/README.md`](./docker-test/README.md) for full details and
troubleshooting.

> **Keystore note (prebuilt images):** the backend uses an Alfresco metadata
> keystore whose password defaults to `changeit`. A **fresh build** from this
> source generates a keystore with that default and needs no extra configuration.
> If you run a **prebuilt image** created with different keystore passwords, pass
> them explicitly:
>
> ```bash
> METADATA_KEYSTORE_PASSWORD=<pwd> METADATA_KEYSTORE_METADATA_PASSWORD=<pwd> \
>   docker compose -f docker-test/docker-compose.yml up -d
> ```

## Default URLs

| What                       | URL                                                                 |
|----------------------------|---------------------------------------------------------------------|
| Frontend (UI)              | <http://localhost:4200/ui>                                          |
| Backend / Alfresco         | <http://localhost:8080/alfresco>                                    |
| Alfresco API explorer      | <http://localhost:8080/api-explorer>                                |
| Solr admin                 | <http://localhost:8983/solr>                                        |
| EU Login mock (discovery)  | <https://localhost:7002/cas/.well-known/openid-configuration>       |

> Running on a remote host? Replace `localhost` with the host's address and make
> sure ports `4200` and `8080` are reachable.

## Default credentials

CIRCABC uses **basic username/password login by default**. (EU Login is optional —
see [EU Login mock](#eu-login-mock).) These are **local development defaults
only** — never use them in production.

| Account | Username | Password | Notes                                             |
|---------|----------|----------|---------------------------------------------------|
| Admin   | `admin`  | `admin`  | CIRCABC/Alfresco administrator                    |

The admin password is configurable via the `CIRCABC_ADMIN_PASSWORD` environment
variable (defaults to `admin`), e.g.:

```bash
CIRCABC_ADMIN_PASSWORD=my-secret docker compose -f docker-test/docker-compose.yml up -d
```

Database (local dev): database `alfresco`, user `alfresco`, password `alfresco`.

## EU Login mock

**By default, CIRCABC uses basic username/password login** — sign in with
`admin` / `admin` (see [Default credentials](#default-credentials)). No EU Login
setup is required to use or test the application.

Production CIRCABC can also authenticate against **EU Login** (an EC CAS identity
provider). For local development this repository ships a **mock CAS provider**
(`docker-test/eulogin-mock/`) implementing the classic CAS endpoints CIRCABC uses
(`/cas/login`, `/cas/laxValidate`).

### Enabling the EU Login mock (optional)

EU Login is only offered in an **enterprise** build flavour (the open-source
default uses basic login). To try it locally:

**1. Start the stack with the EU Login overlay** — it adds the mock, trusts its
certificate, points CAS at it, and runs the backend in enterprise mode
(`build.circabc.release=ent`):

```bash
docker compose \
  -f docker-test/docker-compose.yml \
  -f docker-test/compose.eulogin.yml \
  up -d
```

**2. Enable the enterprise flavour in the frontend** so the UI shows the EU Login
button: set `circabcRelease` to a non-`oss` value (e.g. `'ent'`) in
`frontend/src/environments/environment.next.ts`, then rebuild the frontend
(`... up -d --build frontend`).

Open <http://localhost:4200/ui>, choose **EU Login**, pick a user from the
dropdown (the password is auto-filled), and sign in.

Key properties:

| Where    | Property / setting        | Default                              | Effect |
|----------|---------------------------|--------------------------------------|--------|
| Backend  | `build.circabc.release` (`BUILD_CIRCABC_RELEASE`) | `oss`      | non-`oss` enables the EU Login auth path |
| Backend  | `cas.baseUrl` (`CAS_BASE_URL`)                    | real EU Login | CAS provider base URL (overlay → mock) |
| Backend  | `cas.serviceUrl` / `cas.frontendRedirectUrl`      | localhost   | CAS service + post-login redirect |
| Frontend | `circabcRelease`                                  | `oss`       | non-`oss` shows the EU Login button |

Built-in mock test users (all dev-only):

| Username      | Password    | Name          |
|---------------|-------------|---------------|
| `admin`       | `admin`     | Admin User    |
| `bournja`     | `Admin123`  | Jason Bourne  |
| `chucknorris` | `Qwerty098` | Chuck Norris  |
| `smithja`     | `Test1234`  | Jane Smith    |
| `garciam`     | `Test1234`  | Maria Garcia  |

The mock generates a fresh self-signed certificate at image build time; **no
private key is stored in the repository**. To point at a real EU Login instead,
set `CAS_BASE_URL` (and, if needed, `CAS_SERVICE_URL` /
`CAS_FRONTEND_REDIRECT_URL`). See [`docker-test/README.md`](./docker-test/README.md)
for details. The mock is a development aid, not a complete EU Login implementation.

## Running services individually

### Backend (Alfresco REST API)
```bash
cd backend
mise install           # Java 21 (Temurin) + Maven 3.9
./run.sh build_start   # builds JAR, starts ACS + PostgreSQL + Solr + ActiveMQ
```
Alfresco at `http://localhost:8080`. Stop: `./run.sh stop`. Purge data: `./run.sh purge`.

### Frontend (Angular)
```bash
cd frontend
mise install           # Node 24
npm install
npm start              # ng serve at http://localhost:4200/ui (proxies to :8080)
```
Useful scripts: `npm run tsc`, `npm run lint`, `npm run lint-biome`,
`npm run stylelint`, `npm run format-check`, `npm run circabc-api` (regenerate the
API client from `apis/openapi.yaml`).

### EU Captcha
```bash
cd eu-captcha
mise install
mvn clean package && mvn spring-boot:run
```

## Testing

### End-to-end (Playwright)

The E2E suite is driven by a helper script at the repo root
(`run-e2e-local.sh`) that builds and starts the full stack (backend + frontend)
and runs the Playwright tests against it.

> **Run against a clean database.** The suite is ordered and stateful — it seeds
> users, groups and content once and expects a fresh database. Re-running it
> against a database that already contains that data will fail (e.g. "create
> user" no longer shows *Success*). Always start from a clean seed with
> `down --purge` before a full run.

Recommended full run (all 135 tests; expect ~130 passed, a few intentionally
skipped):

```bash
./run-e2e-local.sh down --purge     # wipe any accumulated data
./run-e2e-local.sh up               # build + start backend (:8080) and frontend (:4200)
./run-e2e-local.sh test -- --max-failures=0   # run the whole suite (don't stop on first failure)
./run-e2e-local.sh down             # stop when finished
```

By default `test` stops at the first failure (`--max-failures=1`); pass
`-- --max-failures=0` to run everything.

Other useful commands:

```bash
./run-e2e-local.sh test tests/10-user-create-page.spec.ts   # run a single spec
./run-e2e-local.sh test 10-user-create                       # run specs matching a name
./run-e2e-local.sh test -- --workers=1                       # run sequentially (fewer flakes)
./run-e2e-local.sh test -- --ui                              # interactive Playwright UI
./run-e2e-local.sh list                                      # list all tests
./run-e2e-local.sh report                                    # open the last HTML report
```

You can also run Playwright directly from `e2e/` once the stack is up:

```bash
cd e2e
npm install
npx playwright install --with-deps
npx playwright test --max-failures=0
```

### Unit tests
- Backend / EU Captcha: `mvn clean verify`
- Frontend: `npm test`

Accessibility matters: the e2e suite includes axe-core audits. Please avoid
introducing accessibility regressions.

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for how
to set up your environment, build and test your changes, and open a pull request.
In short:

1. Fork the repo and create a topic branch from `master`.
2. Make focused changes and run the relevant tests.
3. Open a pull request describing **what** changed, **why**, and how you tested it.
4. Do not include secrets, credentials, or internal infrastructure details.

Report security vulnerabilities privately to the maintainers rather than in public
issues.

## License

Licensed under the **EUPL-1.2** (European Union Public License). See
[LICENSE](./LICENSE).
