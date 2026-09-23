#!/usr/bin/env bash
#
# run-podman-mac.sh
# -----------------
# One-stop script to run the CircaBC backend (Alfresco Content Services) on
# Podman on macOS, following the approach described in the Hyland blog post
# "Using Podman with Alfresco":
#   https://connect.hyland.com/t5/alfresco-blog/using-podman-with-alfresco/ba-p/124875
#
# What it does that the plain `run-podman.sh` does NOT:
#   * Makes sure the Podman CLI and podman-compose are installed (via Homebrew).
#   * Makes sure the Podman machine is provisioned with enough resources.
#     Alfresco needs 16 GB RAM minimum; a fresh Podman machine only gets 2 GB,
#     which is not enough to start ACS + Solr + PostgreSQL. This script bumps an
#     existing machine to 16 GB / 5 CPU non-destructively (via `podman machine
#     set`) or creates a new one with those resources.
#
# Once the environment is ready it builds the Maven module and starts the
# dockerised stack defined in backend/docker/docker-compose.yml through Podman.
#
# Usage: ./run-podman-mac.sh {setup|machine|status|start|build_start|stop|purge|tail|test}
#
set -eu

# --------------------------------------------------------------------------- #
# Configuration                                                               #
# --------------------------------------------------------------------------- #

# Resolve the backend directory (the directory this script lives in) so the
# script works no matter what the current working directory is.
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Podman machine sizing. The blog uses 5 CPUs and 16 GB (16384 MiB) of RAM.
MACHINE_NAME="podman-machine-default"
REQUIRED_CPUS=5
REQUIRED_MEM_MIB=16384
REQUIRED_DISK_GIB=100

# The Maven build filters docker-compose.yml into target/classes/docker.
COMPOSE_FILE_PATH="${SCRIPT_DIR}/target/classes/docker/docker-compose.yml"

# Maven executable (honour M2_HOME like the other run scripts do).
if [ -z "${M2_HOME:-}" ]; then
  MVN_EXEC="mvn"
else
  MVN_EXEC="${M2_HOME}/bin/mvn"
fi

# Skip tests during the build by default (override with SKIP_TESTS=false).
SKIP_TESTS="${SKIP_TESTS:-true}"

# Runtime values consumed by docker-compose.yml via ${VAR} interpolation.
# They are only meaningful for a local dev run, so provide sensible defaults
# unless the caller (or an optional backend/.env file) already set them.
if [ -f "${SCRIPT_DIR}/.env" ]; then
  # shellcheck disable=SC1091
  set -a; . "${SCRIPT_DIR}/.env"; set +a
fi
export CIRCABC_ADMIN_PASSWORD="${CIRCABC_ADMIN_PASSWORD:-admin}"
export IMPORT_PASSWORD="${IMPORT_PASSWORD:-}"
export CAS_SERVICE_URL="${CAS_SERVICE_URL:-}"
export CAS_FRONTEND_REDIRECT_URL="${CAS_FRONTEND_REDIRECT_URL:-}"

# Container runtime / compose command (resolved by detect_compose).
COMPOSE_BIN=""
COMPOSE_SUB=""

# --------------------------------------------------------------------------- #
# Small helpers                                                               #
# --------------------------------------------------------------------------- #

log()  { printf '\033[1;34m==>\033[0m %s\n' "$*"; }
warn() { printf '\033[1;33m[warn]\033[0m %s\n' "$*" >&2; }
die()  { printf '\033[1;31m[error]\033[0m %s\n' "$*" >&2; exit 1; }

has() { command -v "$1" >/dev/null 2>&1; }

# --------------------------------------------------------------------------- #
# Prerequisite tooling (blog: brew install podman / podman-compose)           #
# --------------------------------------------------------------------------- #

ensure_tooling() {
  if ! has brew; then
    die "Homebrew is required but not found. Install it from https://brew.sh and re-run."
  fi

  if ! has podman; then
    log "Podman not found. Installing with Homebrew..."
    brew install podman
  fi
  log "Podman: $(podman --version)"

  # Prefer the built-in `podman compose`; fall back to standalone podman-compose.
  if podman compose version >/dev/null 2>&1; then
    COMPOSE_BIN="podman"; COMPOSE_SUB="compose"
  elif has podman-compose; then
    COMPOSE_BIN="podman-compose"; COMPOSE_SUB=""
  else
    log "podman-compose not found. Installing with Homebrew..."
    brew install podman-compose
    COMPOSE_BIN="podman-compose"; COMPOSE_SUB=""
  fi
  log "Compose command: ${COMPOSE_BIN} ${COMPOSE_SUB}"
}

# Resolve the compose command without triggering installs (used by run actions
# that assume `setup` has already been done at least once).
detect_compose() {
  if podman compose version >/dev/null 2>&1; then
    COMPOSE_BIN="podman"; COMPOSE_SUB="compose"
  elif has podman-compose; then
    COMPOSE_BIN="podman-compose"; COMPOSE_SUB=""
  else
    die "No Podman compose command available. Run '$0 setup' first."
  fi
}

compose() {
  # shellcheck disable=SC2086
  "$COMPOSE_BIN" $COMPOSE_SUB -f "$COMPOSE_FILE_PATH" "$@"
}

# --------------------------------------------------------------------------- #
# Podman machine provisioning (blog: podman machine init --cpus 5 --memory)   #
# --------------------------------------------------------------------------- #

machine_exists() { podman machine inspect "$MACHINE_NAME" >/dev/null 2>&1; }

machine_state() { podman machine inspect "$MACHINE_NAME" --format '{{.State}}' 2>/dev/null || true; }

machine_mem()  { podman machine inspect "$MACHINE_NAME" --format '{{.Resources.Memory}}' 2>/dev/null || echo 0; }

machine_cpus() { podman machine inspect "$MACHINE_NAME" --format '{{.Resources.CPUs}}' 2>/dev/null || echo 0; }

ensure_machine() {
  if ! machine_exists; then
    log "Creating Podman machine '${MACHINE_NAME}' with ${REQUIRED_CPUS} CPUs and $((REQUIRED_MEM_MIB / 1024)) GB RAM..."
    podman machine init \
      --cpus "$REQUIRED_CPUS" \
      --memory "$REQUIRED_MEM_MIB" \
      --disk-size "$REQUIRED_DISK_GIB" \
      "$MACHINE_NAME"
  else
    local cur_mem cur_cpus need_reconfigure
    cur_mem="$(machine_mem)"
    cur_cpus="$(machine_cpus)"
    need_reconfigure="false"
    [ "${cur_mem:-0}" -lt "$REQUIRED_MEM_MIB" ] && need_reconfigure="true"
    [ "${cur_cpus:-0}" -lt "$REQUIRED_CPUS" ] && need_reconfigure="true"

    if [ "$need_reconfigure" = "true" ]; then
      warn "Machine '${MACHINE_NAME}' is under-provisioned (currently ${cur_cpus} CPU / ${cur_mem} MiB)."
      warn "Alfresco needs at least 16 GB RAM. Reconfiguring to ${REQUIRED_CPUS} CPU / ${REQUIRED_MEM_MIB} MiB."
      warn "The machine (and any running containers) will be restarted."
      if [ "$(machine_state)" = "running" ]; then
        log "Stopping machine to apply new resources..."
        podman machine stop "$MACHINE_NAME"
      fi
      podman machine set --cpus "$REQUIRED_CPUS" --memory "$REQUIRED_MEM_MIB" "$MACHINE_NAME"
    else
      log "Machine '${MACHINE_NAME}' already has enough resources (${cur_cpus} CPU / ${cur_mem} MiB)."
    fi
  fi

  if [ "$(machine_state)" != "running" ]; then
    log "Starting Podman machine '${MACHINE_NAME}'..."
    podman machine start "$MACHINE_NAME"
  else
    log "Podman machine '${MACHINE_NAME}' is running."
  fi
}

# --------------------------------------------------------------------------- #
# Backend build & container lifecycle                                         #
# --------------------------------------------------------------------------- #

build() {
  log "Java version: $(java -version 2>&1 | head -1)"
  if [ "$SKIP_TESTS" = "true" ]; then
    log "Building backend (mvn clean package -DskipTests)..."
    (cd "$SCRIPT_DIR" && "$MVN_EXEC" clean package -DskipTests)
  else
    log "Building backend (mvn clean package)..."
    (cd "$SCRIPT_DIR" && "$MVN_EXEC" clean package)
  fi
}

require_compose_file() {
  [ -f "$COMPOSE_FILE_PATH" ] || die "Compose file not found at ${COMPOSE_FILE_PATH}. Build the project first ('$0 build_start')."
}

# Clear a stale "unseen" writeback error on the Podman machine's container
# storage. If the VM's virtual disk ever reports an I/O error, XFS records an
# unseen writeback error and the kernel then refuses every *volatile* overlay
# mount with:
#   "overlayfs: Cannot mount volatile when upperdir has an unseen error.
#    Sync upperdir fs to clear state."
# Since `podman build` (used by `compose up --build`) mounts build containers
# with the volatile option, this breaks every build until the fs is synced.
# Running syncfs marks the error as seen and lets volatile mounts proceed again.
# This is safe and cheap (a no-op when the fs is healthy), and only applies to
# the Podman-machine (applehv) backend on macOS.
clear_overlay_volatile_state() {
  local storage="/var/home/core/.local/share/containers/storage"
  if machine_exists && [ "$(machine_state)" = "running" ]; then
    # First syncfs surfaces and clears any pending unseen writeback error;
    # subsequent volatile overlay mounts then succeed. Ignore its exit status
    # (a reported I/O error here is the stale error being consumed, not a new
    # failure) and don't let it abort the run under `set -e`.
    podman machine ssh "$MACHINE_NAME" "sudo sync -f ${storage} 2>/dev/null; sudo sync" >/dev/null 2>&1 || true
  fi
}

start_containers() {
  require_compose_file
  log "Clearing any stale overlay writeback error on the Podman machine..."
  clear_overlay_volatile_state
  log "Creating persistent volumes..."
  podman volume create circabc-rest-acs-volume >/dev/null
  podman volume create circabc-rest-db-volume  >/dev/null
  podman volume create circabc-rest-ass-volume >/dev/null
  log "Starting containers..."
  compose up --build -d
  log "Backend starting. ACS will be available at http://localhost:8080/alfresco (may take a few minutes)."
}

stop_containers() {
  if [ -f "$COMPOSE_FILE_PATH" ]; then
    log "Stopping containers..."
    compose down
  fi
}

purge_volumes() {
  log "Removing persistent volumes..."
  podman volume rm -f circabc-rest-acs-volume circabc-rest-db-volume circabc-rest-ass-volume
}

tail_logs()     { require_compose_file; compose logs -f; }
tail_logs_all() { require_compose_file; compose logs --tail=all; }

run_tests() { (cd "$SCRIPT_DIR" && "$MVN_EXEC" verify); }

status() {
  log "Podman: $(has podman && podman --version || echo 'not installed')"
  if machine_exists; then
    log "Machine '${MACHINE_NAME}': state=$(machine_state), cpus=$(machine_cpus), memory=$(machine_mem) MiB"
    if [ "$(machine_mem)" -lt "$REQUIRED_MEM_MIB" ]; then
      warn "Memory is below the 16 GB Alfresco minimum. Run '$0 machine' to fix it."
    fi
  else
    warn "Machine '${MACHINE_NAME}' does not exist. Run '$0 setup'."
  fi
  if [ -f "$COMPOSE_FILE_PATH" ]; then
    detect_compose
    compose ps || true
  else
    warn "Compose file not built yet (run '$0 build_start')."
  fi
}

usage() {
  cat <<EOF
Usage: $0 <command>

Environment setup (from the "Using Podman with Alfresco" blog):
  setup         Ensure Podman + podman-compose are installed and the Podman
                machine has 5 CPU / 16 GB RAM, then start the machine.
  machine       Only provision/adjust and start the Podman machine.
  status        Show Podman, machine and container status.

Run the backend:
  build_start   Full flow: setup env, mvn build, then start containers + tail logs.
  start         Start containers without rebuilding, then tail logs.
  stop          Stop and remove the containers.
  purge         Stop containers and delete all persistent volumes (data loss).
  tail          Tail container logs.
  test          Run the Maven integration tests (environment must be up).

Env vars: SKIP_TESTS=false to build with tests; CIRCABC_ADMIN_PASSWORD,
IMPORT_PASSWORD, CAS_SERVICE_URL, CAS_FRONTEND_REDIRECT_URL override compose
values (an optional backend/.env file is sourced automatically).
EOF
}

# --------------------------------------------------------------------------- #
# Entry point                                                                 #
# --------------------------------------------------------------------------- #

case "${1:-}" in
  setup)
    ensure_tooling
    ensure_machine
    log "Setup complete. Run '$0 build_start' to build and launch the backend."
    ;;
  machine)
    ensure_machine
    ;;
  status)
    status
    ;;
  build_start)
    ensure_tooling
    ensure_machine
    stop_containers
    build
    start_containers
    tail_logs
    ;;
  start)
    ensure_tooling
    ensure_machine
    start_containers
    tail_logs
    ;;
  stop)
    detect_compose
    stop_containers
    ;;
  purge)
    detect_compose
    stop_containers
    purge_volumes
    ;;
  tail)
    detect_compose
    tail_logs
    ;;
  test)
    run_tests
    ;;
  *)
    usage
    exit 1
    ;;
esac
