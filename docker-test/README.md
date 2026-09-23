# CIRCABC — Local Docker Test Environment

This directory provides a self-contained environment to evaluate CIRCABC on your
machine, including a **mock EU Login provider** so you can exercise the login flow
without access to the real EU Login / CAS service.

> This environment is for **local testing and evaluation only**. It uses
> throwaway credentials and self-signed certificates and must never be exposed
> publicly or used in production.

## Services

| Service        | Port                  | Description                                   |
|----------------|-----------------------|-----------------------------------------------|
| `circabc-rest-acs`      | `8080`       | Alfresco Content Services + CIRCABC REST API  |
| `frontend`     | `4200`                | Angular UI dev server                         |
| `circabc-rest-postgres` | `5432`       | PostgreSQL database                           |
| `circabc-rest-ass`      | `8983`       | Alfresco Search Services (Solr)               |
| `eulogin-mock` | `7002` (HTTPS)        | Mock EU Login / CAS OAuth2 provider           |

## Prerequisites

- Docker 24+ with the Compose plugin (`docker compose`)
- The backend image built once locally (it packages the Alfresco WAR):

  ```bash
  cd ../backend
  ./run.sh build_start   # builds the JAR and the backend Docker image
  ```

  See [`../backend/README.md`](../backend/README.md) for details and
  alternatives (Podman, etc.).

## Run

```bash
docker compose -f docker-test/docker-compose.yml up -d
```

Then open:

- Frontend UI: <http://localhost:4200>
- Backend / Alfresco: <http://localhost:8080/alfresco>
- EU Login mock discovery: <https://localhost:7002/cas/.well-known/openid-configuration>
  (self-signed certificate — accept the browser warning)

Stop and clean up:

```bash
docker compose -f docker-test/docker-compose.yml down -v
```

## EU Login mock

Production CIRCABC authenticates against **EU Login** (an EC CAS identity
provider). **By default this test environment uses basic username/password login**
(`admin` / `admin`) — no EU Login is involved and you do not need the mock.

If you want to exercise the EU Login flow locally, this repository ships a mock
CAS provider (`docker-test/eulogin-mock/`) implementing the classic CAS endpoints
CIRCABC uses (`/cas/login` and `/cas/laxValidate`).

### Enabling the EU Login mock

EU Login is only offered when CIRCABC runs in an **enterprise** build flavour
(the open-source default uses basic login). Enabling it therefore requires two
things: switching the release flavour to enterprise, and pointing CAS at the mock.

**1. Start the stack with the EU Login overlay.** This adds the mock, imports its
certificate into the backend truststore, points `cas.baseUrl` at the mock, and
switches the backend to the enterprise flavour (`build.circabc.release=ent`) so
the EU Login path is active:

```bash
docker compose \
  -f docker-test/docker-compose.yml \
  -f docker-test/compose.eulogin.yml \
  up -d
```

**2. Enable the enterprise flavour in the frontend** so the UI shows the EU Login
option. Set `circabcRelease` to a non-`oss` value (e.g. `'ent'`) in the
environment file used by your serve/build target
(`frontend/src/environments/environment.next.ts`), then rebuild the frontend:

```bash
docker compose \
  -f docker-test/docker-compose.yml \
  -f docker-test/compose.eulogin.yml \
  up -d --build frontend
```

Then open <http://localhost:4200/ui> and choose the **EU Login** option; you will
be redirected to the mock login page. Pick a user from the dropdown (the password
is filled in automatically) and sign in.

### Relevant properties

| Property (backend) | Env var | Default | Purpose |
|--------------------|---------|---------|---------|
| `build.circabc.release` | `BUILD_CIRCABC_RELEASE` | `oss` | `ent` (any non-`oss`) enables the EU Login auth path |
| `cas.baseUrl` | `CAS_BASE_URL` | `https://ecas.ec.europa.eu/cas` | CAS provider base URL (the overlay sets it to the mock) |
| `cas.serviceUrl` | `CAS_SERVICE_URL` | `http://localhost:8080/alfresco/service/circabc/eulogin` | Service URL the CAS ticket is issued for |
| `cas.frontendRedirectUrl` | `CAS_FRONTEND_REDIRECT_URL` | `http://localhost:4200/ui/welcome` | Where the user lands after login |

| Setting (frontend) | File | Default | Purpose |
|--------------------|------|---------|---------|
| `circabcRelease` | `environment.*.ts` | `oss` | non-`oss` shows the EU Login button |
| `euloginUrl` | `environment.*.ts` | `.../service/circabc/eulogin` | URL the EU Login button calls |

> **Remote host?** When accessing from another machine, replace `localhost` in
> `CAS_SERVICE_URL`, `CAS_FRONTEND_REDIRECT_URL` and the frontend `euloginUrl`
> with the host's address, and set `CAS_BASE_URL` to the host address too (the
> mock's certificate must then include that address — rebuild the mock with
> `--build-arg EXTRA_SANS=<host-or-ip>`).

Mock test users (dev-only):

| Username      | Password    | Name          |
|---------------|-------------|---------------|
| `admin`       | `admin`     | Admin User    |
| `bournja`     | `Admin123`  | Jason Bourne  |
| `chucknorris` | `Qwerty098` | Chuck Norris  |
| `smithja`     | `Test1234`  | Jane Smith    |
| `garciam`     | `Test1234`  | Maria Garcia  |

Notes:

- **The default remains basic username/password login** (`admin` / `admin`); the
  steps above are only needed to try the EU Login flow.
- The mock generates a fresh self-signed certificate at image build time; **no
  private key is committed to the repository**.
- To point at a real EU Login instead, set `CAS_BASE_URL` (and, if needed,
  `CAS_SERVICE_URL` / `CAS_FRONTEND_REDIRECT_URL`).
- The mock is a development aid, not a complete EU Login implementation.

## Troubleshooting

- **Backend image not found**: build it first (see Prerequisites).
- **Keystore fails to initialize (prebuilt image)**: the Alfresco metadata
  keystore password defaults to `changeit`. A fresh build from source generates a
  keystore with that default. If you use a prebuilt image created with different
  keystore passwords, pass them explicitly:
  ```bash
  METADATA_KEYSTORE_PASSWORD=<pwd> METADATA_KEYSTORE_METADATA_PASSWORD=<pwd> \
    docker compose -f docker-test/docker-compose.yml up -d
  ```
- **Solr not indexing**: ensure `circabc-rest-acs` and `circabc-rest-ass` share the
  `secret` shared secret (already set in the compose file).
- **TLS warnings from the mock**: expected — the certificate is self-signed.
