#!/bin/sh

export COMPOSE_FILE_PATH="${PWD}/target/classes/docker/docker-compose.yml"

if [ -z "${M2_HOME}" ]; then
  MVN_EXEC="mvn"
else
  MVN_EXEC="${M2_HOME}/bin/mvn"
fi

DOCKER_CMD=""
COMPOSE_BIN=""
COMPOSE_SUB=""

detect_runtime() {
    if command -v docker >/dev/null 2>&1; then
        DOCKER_CMD="docker"
        if docker compose version >/dev/null 2>&1; then
            COMPOSE_BIN="docker"
            COMPOSE_SUB="compose"
        elif command -v docker-compose >/dev/null 2>&1; then
            COMPOSE_BIN="docker-compose"
            COMPOSE_SUB=""
        else
            echo "Docker found but Compose is missing. Install docker-compose-plugin or docker-compose." >&2
            exit 1
        fi
        return
    fi

    if command -v podman >/dev/null 2>&1; then
        DOCKER_CMD="podman"
        if podman compose version >/dev/null 2>&1; then
            COMPOSE_BIN="podman"
            COMPOSE_SUB="compose"
        elif command -v podman-compose >/dev/null 2>&1; then
            COMPOSE_BIN="podman-compose"
            COMPOSE_SUB=""
        else
            echo "Podman found but compose is missing. Install podman-compose." >&2
            exit 1
        fi
        return
    fi

    echo "No container runtime found. Install Docker (recommended) or Podman and try again." >&2
    exit 1
}

start() {
    "$DOCKER_CMD" volume create circabc-rest-acs-volume
    "$DOCKER_CMD" volume create circabc-rest-db-volume
    "$DOCKER_CMD" volume create circabc-rest-ass-volume
    "$COMPOSE_BIN" $COMPOSE_SUB -f "$COMPOSE_FILE_PATH" up --build -d
}

down() {
    if [ -f "$COMPOSE_FILE_PATH" ]; then
    # Preserve networks: stop containers and remove them without tearing down networks
    "$COMPOSE_BIN" $COMPOSE_SUB -f "$COMPOSE_FILE_PATH" stop
    "$COMPOSE_BIN" $COMPOSE_SUB -f "$COMPOSE_FILE_PATH" rm -f
    fi
}

purge() {
    "$DOCKER_CMD" volume rm -f circabc-rest-acs-volume
    "$DOCKER_CMD" volume rm -f circabc-rest-db-volume
    "$DOCKER_CMD" volume rm -f circabc-rest-ass-volume
}

build() {
    JAVA_VERSION=$(java -version 2>&1)
    echo "Java version: $JAVA_VERSION"
    "$MVN_EXEC" clean package -DskipTests
}

tail() {
    "$COMPOSE_BIN" $COMPOSE_SUB -f "$COMPOSE_FILE_PATH" logs -f
}

tail_all() {
    "$COMPOSE_BIN" $COMPOSE_SUB -f "$COMPOSE_FILE_PATH" logs --tail="all"
}

prepare_test() {
    "$MVN_EXEC" verify -DskipTests=true
}

test() {
    "$MVN_EXEC" verify
}

detect_runtime

case "$1" in
  build_start)
    down
    build
    start
    tail
    ;;
  build_start_it_supported)
    down
    build
    prepare_test
    start
    tail
    ;;
  start)
    start
    tail
    ;;
  stop)
    down
    ;;
  purge)
    down
    purge
    ;;
  tail)
    tail
    ;;
  build_test)
    down
    build
    prepare_test
    start
    test
    tail_all
    down
    ;;
  test)
    test
    ;;
  *)
    echo "Usage: $0 {build_start|build_start_it_supported|start|stop|purge|tail|build_test|test}"
    ;;
esac