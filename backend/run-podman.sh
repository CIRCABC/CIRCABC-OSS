#!/bin/sh

export COMPOSE_FILE_PATH="${PWD}/target/classes/docker/docker-compose.yml"

if [ -z "${M2_HOME}" ]; then
  export MVN_EXEC="mvn"
else
  export MVN_EXEC="${M2_HOME}/bin/mvn"
fi

start() {
    podman volume create circabc-rest-acs-volume
    podman volume create circabc-rest-db-volume
    podman volume create circabc-rest-ass-volume
    podman compose -f "$COMPOSE_FILE_PATH" up --build -d
}

down() {
    if [ -f "$COMPOSE_FILE_PATH" ]; then
        podman compose -f "$COMPOSE_FILE_PATH" down
    fi
}

purge() {
    podman volume rm -f circabc-rest-acs-volume
    podman volume rm -f circabc-rest-db-volume
    podman volume rm -f circabc-rest-ass-volume
}

build() {
    JAVA_VERSION=$(java -version 2>&1)
    echo "Java version: $JAVA_VERSION"
    $MVN_EXEC clean package
}

tail() {
    podman compose -f "$COMPOSE_FILE_PATH" logs -f
}

tail_all() {
    podman compose -f "$COMPOSE_FILE_PATH" logs --tail="all"
}

prepare_test() {
    $MVN_EXEC verify -DskipTests=true
}

test() {
    $MVN_EXEC verify
}

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
esac