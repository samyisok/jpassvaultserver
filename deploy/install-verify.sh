#!/usr/bin/env bash
#
# Verifies deploy/install.sh in a throwaway container: it runs the installer and
# proves the service starts as the service user from the installed paths.
#
# The container has no init system, so `systemctl` is shimmed to a no-op. The
# systemd unit is verified separately with `systemd-analyze verify` (run below).
# Requires Docker and a built jar (./gradlew bootJar).
#
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
JAR_PATH="$(ls "$ROOT"/build/libs/jpassvaultserver-*.jar | grep -v -- '-plain' | head -1)"
IMAGE="${IMAGE:-eclipse-temurin:25-jre}"
SECRET="install-verify-secret-0123456789"

if command -v systemd-analyze >/dev/null 2>&1; then
  systemd-analyze verify "$ROOT/deploy/jpassvaultserver.service"
  echo "systemd unit verified"
fi

echo "Verifying install.sh with $IMAGE using $(basename "$JAR_PATH")"

docker run --rm \
  -v "$ROOT:/src:ro,z" -w /src \
  -e JAR_NAME="$(basename "$JAR_PATH")" \
  -e SECRET="$SECRET" \
  "$IMAGE" bash -euo pipefail -c '
    mkdir -p /tmp/bin
    printf "#!/bin/sh\nexit 0\n" > /tmp/bin/systemctl
    chmod +x /tmp/bin/systemctl
    export PATH=/tmp/bin:$PATH

    bash deploy/install.sh "build/libs/$JAR_NAME"

    id jpassvaultserver >/dev/null
    [ "$(stat -c %a /var/lib/jpassvaultserver/data)" = "700" ]
    [ "$(stat -c %a /opt/jpassvaultserver/jpassvaultserver.jar)" = "644" ]
    [ "$(stat -c %a /etc/jpassvaultserver.env)" = "600" ]
    [ -f /etc/systemd/system/jpassvaultserver.service ]
    echo "installer file layout verified"

    command -v curl >/dev/null 2>&1 || {
      apt-get update >/dev/null 2>&1
      apt-get install -y --no-install-recommends curl >/dev/null 2>&1
    }
    JAVA_BIN="$(command -v java)"

    su -s /bin/bash jpassvaultserver -c \
      "JPASSVAULT_SECRET=$SECRET APP_PROPERTIES_TLS_TERMINATED_AT_PROXY=true SPRING_DATASOURCE_URL=jdbc:h2:file:/var/lib/jpassvaultserver/data/maindb $JAVA_BIN -jar /opt/jpassvaultserver/jpassvaultserver.jar --server.port=9393" &
    PID=$!

    code=""
    for _ in $(seq 1 60); do
      code=$(curl -s -o /dev/null -w "%{http_code}" -H "token: $SECRET" http://127.0.0.1:9393/check 2>/dev/null || true)
      [ "$code" = "200" ] && break
      sleep 1
    done
    [ "$code" = "200" ] || { echo "service did not become ready (last=$code)"; exit 1; }
    echo "service ready as user: $(id -un jpassvaultserver)"
    kill "$PID" 2>/dev/null || true
  '

echo "install-verify: PASS"
