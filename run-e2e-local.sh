#!/usr/bin/env bash
#
# run-e2e-local.sh — Bring up the full CircaBC stack locally and run the Playwright E2E suite.
#
# Orchestrates the three moving parts the E2E tests need:
#   1. Backend  : Alfresco stack (ACS + Postgres + Solr) via backend/run.sh, on :8080
#   2. Frontend : Angular dev server with the `next` config (proxy -> :8080, versioned i18n), on :4200
#   3. E2E      : Playwright test suite in e2e/
#
# The `next` serve config is mandatory: plain `npm start` points at :7001 and does NOT
# generate the version-scoped translation assets, so the UI renders empty and every test fails.
#
# Usage:
#   ./run-e2e-local.sh [up|test|report|list|down|all] [args] [-- <extra playwright args>]
#
#   up      Build + start backend, wait for readiness, start frontend. Leaves them running.
#   test    Run the E2E suite (assumes `up` already ran). Optionally pass spec files or a
#           name pattern to run a subset; anything after `--` is forwarded to `playwright test`.
#   report  Open the HTML report from the last test run in a browser.
#   list    List every discovered test without running anything.
#   down    Stop frontend + backend stack (keeps data volumes; use --purge to wipe them).
#   all     up -> test -> down  (default)
#
# Examples:
#   ./run-e2e-local.sh                                     # full cycle: up, test, down
#   ./run-e2e-local.sh up                                  # just bring the environment up
#   ./run-e2e-local.sh test                                # run the whole suite (stops on 1st failure)
#   ./run-e2e-local.sh test tests/10-user-create-page.spec.ts   # run one spec
#   ./run-e2e-local.sh test 10-user-create                 # run specs matching a filename pattern
#   ./run-e2e-local.sh test -- --headed                    # watch the browser while it runs
#   ./run-e2e-local.sh test -- --workers=1                 # run sequentially (no parallel flakes)
#   ./run-e2e-local.sh test -- --ui                        # interactive Playwright UI mode
#   ./run-e2e-local.sh test -- --max-failures=0            # run everything, don't stop on failures
#   ./run-e2e-local.sh report                              # open the last HTML report
#   ./run-e2e-local.sh list                                # list all tests
#   ./run-e2e-local.sh down --purge                        # tear down and delete Alfresco data volumes
#
# Env overrides:
#   CIRCABC_ADMIN_PASSWORD (default: admin)
#   ACS_DEBUG_PORT         (default: 8890)  host port mapped to the JVM debug port 8888
#   STOP_CONFLICTING=1     stop known conflicting containers (eushare_*, angular, tomcat, db) on `up`
#
set -euo pipefail

# ---------------------------------------------------------------------------
# Paths & config
# ---------------------------------------------------------------------------
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$REPO_ROOT/backend"
FRONTEND_DIR="$REPO_ROOT/frontend"
E2E_DIR="$REPO_ROOT/e2e"

COMPOSE_FILE="$BACKEND_DIR/target/classes/docker/docker-compose.yml"
FRONTEND_LOG="/tmp/circabc-frontend-next.log"
FRONTEND_PID_FILE="/tmp/circabc-frontend-next.pid"

export CIRCABC_ADMIN_PASSWORD="${CIRCABC_ADMIN_PASSWORD:-admin}"
export IMPORT_PASSWORD="${IMPORT_PASSWORD:-}"
export CAS_SERVICE_URL="${CAS_SERVICE_URL:-}"
export CAS_FRONTEND_REDIRECT_URL="${CAS_FRONTEND_REDIRECT_URL:-}"
ACS_DEBUG_PORT="${ACS_DEBUG_PORT:-8890}"

ACS_READY_URL="http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/-root-"
FRONTEND_URL="http://localhost:4200/ui"

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
log()  { printf '\n\033[1;34m==>\033[0m %s\n' "$*"; }
warn() { printf '\033[1;33m[warn]\033[0m %s\n' "$*"; }
die()  { printf '\033[1;31m[error]\033[0m %s\n' "$*" >&2; exit 1; }

# Load mise-managed toolchain (Java 21 / Node 24) if mise is available.
load_mise() {
  if command -v mise >/dev/null 2>&1; then
    eval "$(mise env -s bash 2>/dev/null)" || true
  fi
}

# Run a command through the mise-pinned toolchain when mise is present, so the
# correct Node/Java versions are guaranteed regardless of the caller's PATH.
# Falls back to running the command directly if mise is unavailable.
mrun() {
  if command -v mise >/dev/null 2>&1; then
    mise exec -- "$@"
  else
    "$@"
  fi
}

# Fail fast if the resolved Node major version is too old for Angular.
require_node() {
  local ver major
  ver="$(mrun node --version 2>/dev/null | sed 's/^v//')" || true
  major="${ver%%.*}"
  if [ -z "$major" ] || [ "$major" -lt 22 ] 2>/dev/null; then
    die "Node $ver is too old (Angular needs >=22). Install via: (cd $1 && mise install)"
  fi
  log "Using Node v$ver"
}

require_docker() {
  command -v docker >/dev/null 2>&1 || die "docker not found. Install Docker (or adapt to podman)."
  docker compose version >/dev/null 2>&1 || die "docker compose plugin not found."
}

stop_conflicting() {
  log "Stopping known conflicting containers (ports 8080/5555/8983)…"
  docker stop eushare_dev_proxy angular tomcat db 2>/dev/null || true
}

# ---------------------------------------------------------------------------
# up: build + start backend, wait, start frontend
# ---------------------------------------------------------------------------
cmd_up() {
  require_docker
  load_mise

  [ "${STOP_CONFLICTING:-0}" = "1" ] && stop_conflicting

  log "Building + starting backend (Alfresco) stack…"
  cd "$BACKEND_DIR"
  # Build JAR + ACS image first so the filtered compose file exists, then remap debug port.
  ./run.sh build >/dev/null 2>&1 || ./run.sh build
  [ -f "$COMPOSE_FILE" ] || die "Compose file not generated at $COMPOSE_FILE"
  if [ "$ACS_DEBUG_PORT" != "8888" ]; then
    sed -i "s#- \"8888:8888\"#- \"$ACS_DEBUG_PORT:8888\"#" "$COMPOSE_FILE"
  fi
  docker volume create circabc-rest-acs-volume >/dev/null
  docker volume create circabc-rest-db-volume  >/dev/null
  docker volume create circabc-rest-ass-volume >/dev/null
  docker compose -f "$COMPOSE_FILE" up --build -d

  log "Waiting for Alfresco to become ready (this can take several minutes)…"
  local ready=0
  for i in $(seq 1 90); do
    code="$(curl -s -o /dev/null -w '%{http_code}' "$ACS_READY_URL" 2>/dev/null || echo 000)"
    if [ "$code" = "401" ] || [ "$code" = "200" ]; then
      log "Alfresco ready after ~$((i*10))s (HTTP $code)."
      ready=1; break
    fi
    sleep 10
  done
  [ "$ready" = "1" ] || die "Alfresco did not become ready in time. Check: cd backend && ./run.sh tail"

  log "Starting frontend dev server (next config) on :4200…"
  cd "$FRONTEND_DIR"
  require_node "$FRONTEND_DIR"
  [ -d node_modules ] || mrun npm install
  # nohup cannot run a shell function, so resolve the mise wrapper to a concrete
  # command prefix and background that instead.
  local MISE_PREFIX=""
  command -v mise >/dev/null 2>&1 && MISE_PREFIX="mise exec --"
  nohup $MISE_PREFIX npm run next -- --host 0.0.0.0 --port 4200 > "$FRONTEND_LOG" 2>&1 &
  echo $! > "$FRONTEND_PID_FILE"

  log "Waiting for frontend to serve…"
  local fready=0
  for i in $(seq 1 45); do
    code="$(curl -s -o /dev/null -w '%{http_code}' "$FRONTEND_URL" 2>/dev/null || echo 000)"
    if [ "$code" = "200" ]; then
      log "Frontend ready after ~$((i*8))s."
      fready=1; break
    fi
    sleep 8
  done
  [ "$fready" = "1" ] || { tail -20 "$FRONTEND_LOG"; die "Frontend did not come up. See $FRONTEND_LOG"; }

  # Sanity: versioned translations must resolve or the UI renders empty.
  tcode="$(curl -s -o /dev/null -w '%{http_code}' "$FRONTEND_URL/assets/5.0.0.0/i18n/en.json" 2>/dev/null || echo 000)"
  [ "$tcode" = "200" ] && log "Translations OK (versioned assets served)." \
                       || warn "Versioned translations returned HTTP $tcode — UI text may be empty."

  log "Environment is up. Backend :8080 · Frontend $FRONTEND_URL"
}

# ---------------------------------------------------------------------------
# Verify the frontend (and therefore the backend it proxies to) is reachable
# before running any tests, so we fail fast with a clear message instead of a
# wall of Playwright timeouts when the environment isn't up.
# ---------------------------------------------------------------------------
check_env_ready() {
  local code
  code="$(curl -s -o /dev/null -w '%{http_code}' "$FRONTEND_URL" 2>/dev/null || echo 000)"
  if [ "$code" != "200" ]; then
    die "Frontend not reachable at $FRONTEND_URL (HTTP $code). Bring the stack up first: ./run-e2e-local.sh up"
  fi
}

# ---------------------------------------------------------------------------
# test: run the playwright suite
#
# Usage:
#   ./run-e2e-local.sh test [spec|pattern ...] [-- <extra playwright args>]
#
#   No args           -> full suite, stops on first failure (npm test)
#   Spec/pattern args -> run only those specs, e.g.
#                        ./run-e2e-local.sh test tests/10-user-create-page.spec.ts
#                        ./run-e2e-local.sh test 10-user-create
#   Everything after `--` is forwarded verbatim to `playwright test`, e.g.
#                        ./run-e2e-local.sh test -- --headed --workers=1
# ---------------------------------------------------------------------------
cmd_test() {
  load_mise
  cd "$E2E_DIR"
  require_node "$E2E_DIR"
  [ -d node_modules ] || mrun npm install
  # Ensure the browser is present; install if the version mismatches.
  mrun npx playwright install chromium >/dev/null 2>&1 || mrun npx playwright install --with-deps chromium

  check_env_ready

  log "Running Playwright E2E suite…"
  if [ "$#" -gt 0 ]; then
    mrun npx playwright test "$@"
  else
    mrun npm test
  fi
}

# ---------------------------------------------------------------------------
# report: open the last Playwright HTML report in a browser
# ---------------------------------------------------------------------------
cmd_report() {
  load_mise
  cd "$E2E_DIR"
  require_node "$E2E_DIR"
  log "Opening the latest Playwright HTML report…"
  mrun npx playwright show-report
}

# ---------------------------------------------------------------------------
# list: print every discovered test without running anything
# ---------------------------------------------------------------------------
cmd_list() {
  load_mise
  cd "$E2E_DIR"
  require_node "$E2E_DIR"
  [ -d node_modules ] || mrun npm install
  mrun npx playwright test --list
}

# ---------------------------------------------------------------------------
# down: stop frontend + backend
# ---------------------------------------------------------------------------
cmd_down() {
  local purge=0
  [ "${1:-}" = "--purge" ] && purge=1

  log "Stopping frontend dev server…"
  if [ -f "$FRONTEND_PID_FILE" ]; then
    kill "$(cat "$FRONTEND_PID_FILE")" 2>/dev/null || true
    rm -f "$FRONTEND_PID_FILE"
  fi
  pkill -f "ng serve" 2>/dev/null || true
  pkill -f "@angular/build:dev-server" 2>/dev/null || true

  log "Stopping backend stack…"
  cd "$BACKEND_DIR"
  if [ "$purge" = "1" ]; then
    ./run.sh purge || true
    log "Data volumes purged (fresh seed on next up)."
  else
    ./run.sh stop || true
  fi
}

# ---------------------------------------------------------------------------
# Arg parsing: <command> [-- <passthrough args>]
# ---------------------------------------------------------------------------
COMMAND="${1:-all}"; shift || true
PASSTHROUGH=()
if [ "${1:-}" = "--" ]; then shift; PASSTHROUGH=("$@"); else PASSTHROUGH=("$@"); fi

case "$COMMAND" in
  up)     cmd_up ;;
  test)   cmd_test "${PASSTHROUGH[@]}" ;;
  report) cmd_report ;;
  list)   cmd_list ;;
  down)   cmd_down "${PASSTHROUGH[@]}" ;;
  all)
    cmd_up
    set +e; cmd_test "${PASSTHROUGH[@]}"; rc=$?; set -e
    cmd_down
    exit $rc
    ;;
  *)
    grep -E '^#( |$)' "$0" | sed -E 's/^# ?//'
    exit 1
    ;;
esac
