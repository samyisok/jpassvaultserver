#!/usr/bin/env bash
#
# Install the jpassvault sync server as a hardened systemd service.
# Run as root. Usage: install.sh [path-to-jar]
#
set -euo pipefail

JAR_SOURCE="${1:-build/libs/jpassvaultserver-2.1.0.jar}"
SERVICE_USER="jpassvaultserver"
INSTALL_DIR="/opt/jpassvaultserver"
DATA_DIR="/var/lib/jpassvaultserver"
ENV_FILE="/etc/jpassvaultserver.env"
UNIT_FILE="/etc/systemd/system/jpassvaultserver.service"

if [ "$(id -u)" -ne 0 ]; then
  echo "This installer must be run as root." >&2
  exit 1
fi

if [ ! -f "$JAR_SOURCE" ]; then
  echo "Jar not found: $JAR_SOURCE" >&2
  exit 1
fi

if ! id "$SERVICE_USER" >/dev/null 2>&1; then
  useradd --system --home-dir "$DATA_DIR" --shell /usr/sbin/nologin "$SERVICE_USER"
fi

install -d -m 0755 "$INSTALL_DIR"
install -m 0644 "$JAR_SOURCE" "$INSTALL_DIR/jpassvaultserver.jar"

install -d -m 0700 -o "$SERVICE_USER" -g "$SERVICE_USER" "$DATA_DIR"
install -d -m 0700 -o "$SERVICE_USER" -g "$SERVICE_USER" "$DATA_DIR/data"

if [ ! -f "$ENV_FILE" ]; then
  install -m 0600 "$(dirname "$0")/jpassvaultserver.env.example" "$ENV_FILE"
  echo "Created $ENV_FILE — edit it and set JPASSVAULT_SECRET before starting."
fi

install -m 0644 "$(dirname "$0")/jpassvaultserver.service" "$UNIT_FILE"

systemctl daemon-reload
systemctl enable jpassvaultserver.service
echo "Installed. Edit $ENV_FILE, then: systemctl start jpassvaultserver"
