# Implementation Plan — Runtime Environment Configuration for Docker

## Problem Statement
The Angular frontend currently bakes environment configuration into the JS bundle at build time via `environment.prod.ts` file replacement. This means a separate Docker image must be built for each environment. The goal is to make the Docker image environment-agnostic by loading configuration from environment variables at container startup.

## Requirements
- All 25 properties from the `Environment` interface must be overridable via container env vars
- Default values come from the current `environment.prod.ts` (the `ENVIRONMENT_TS` GitLab CI File variable)
- The existing `bitnamilegacy/nginx:1.29` Docker image is kept
- Minimal changes to the 46 files that import `environment` — the `environment` object stays as the single source of truth
- The `docker-build-frontend` CI job generates the Dockerfile inline, so changes go there

## Proposed Solution
At container startup, an entrypoint shell script reads environment variables and writes a `config.json` into the nginx html directory. The Angular app's `main.ts` fetches `config.json` before bootstrapping and merges the values into the mutable `environment` object. Since `environment` is a module-level `const` object (but its properties are mutable), we can `Object.assign()` runtime values onto it before Angular bootstraps — no `APP_INITIALIZER` needed, and no changes to the 46 consumer files.

## Key Design Decisions
- `main.ts` uses `fetch()` before `bootstrapApplication()` — this ensures `environment` is populated before any Angular code runs
- The entrypoint script maps env vars with a `CIRCABC_` prefix to `Environment` interface keys (e.g., `CIRCABC_PRODUCTION=true` → `environment.production = true`)
- Build-time defaults from `environment.prod.ts` remain as fallbacks in the compiled bundle — `config.json` only overrides what's explicitly set

## Environment Variable Mapping

| Environment Variable | Environment Interface Key |
|---|---|
| CIRCABC_PRODUCTION | production |
| CIRCABC_ALFRESCO_URL | alfrescoURL |
| CIRCABC_CIRCABC_URL | circabcURL |
| CIRCABC_SERVER_URL | serverURL |
| CIRCABC_ALFRESCO_HOST | alfrescoHost |
| CIRCABC_BASE_HREF | baseHref |
| CIRCABC_NODE_NAME | nodeName |
| CIRCABC_SHOW_UI_SWITCH | showUiSwitch |
| CIRCABC_ENVIRONMENT_TYPE | environmentType |
| CIRCABC_CIRCABC_RELEASE | circabcRelease |
| CIRCABC_ARES_BRIDGE_ENABLED | aresBridgeEnabled |
| CIRCABC_ARES_BRIDGE_SERVER | aresBridgeServer |
| CIRCABC_ARES_BRIDGE_URL | aresBridgeURL |
| CIRCABC_ARES_BRIDGE_KEY | aresBridgeKey |
| CIRCABC_ARES_BRIDGE_UI_URL | aresBridgeUiURL |
| CIRCABC_ANALYTICS_URL | analyticsURL |
| CIRCABC_ANALYTICS_SITE_ID | analyticsSiteId |
| CIRCABC_ANALYTICS_INSTANCE | analyticsInstance |
| CIRCABC_OFFICE_CLIENT_ID | officeClientId |
| CIRCABC_SHARE_URL | shareURL |
| CIRCABC_CAPTCHA_URL | captchaURL |
| CIRCABC_EULOGIN_URL | euloginUrl |
| CIRCABC_EULOGOUT_URL | eulogoutUrl |
| CIRCABC_USE_ALFRESCO_API | useAlfrescoAPI |

## Task Breakdown

### Task 1: Create the runtime config loading in `main.ts`
- Modify `main.ts` to fetch `config.json` before bootstrapping and merge values into the `environment` object
- Handle type coercion for booleans
- On fetch failure, log warning and continue with build-time defaults

### Task 2: Create the Docker entrypoint script
- Create `docker/entrypoint.sh` that generates `config.json` from `CIRCABC_*` environment variables
- Only include explicitly set env vars in the JSON
- Handle boolean vs string types correctly

### Task 3: Update the CI `docker-build-frontend` job
- Modify the inline Dockerfile in `.gitlab-ci.yml` to include and use `entrypoint.sh`

### Task 4: Simplify CI pipeline
- Remove `ENVIRONMENT_TS` handling from `setup-environment`
- Remove `frontend/src/environments/environment.prod.ts` from artifacts
- Remove `setup-environment` from `build-frontend` dependencies
