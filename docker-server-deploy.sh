#!/usr/bin/env bash

set -euo pipefail

DEPLOY_DIR="$(cd "$(dirname "$0")" && pwd)"
ACTION="${1:-up}"
COMPOSE_FILE="$DEPLOY_DIR/docker-compose.yml"
LOG_SERVICES=(
  exchange-gateway
  exchange-web
  exchange-admin
  exchange-auth
  exchange-business-user
  exchange-business-account
  exchange-business-market
  exchange-datasource
  exchange-resource
)

run_with_timeout() {
  local seconds="$1"
  shift
  if command -v timeout >/dev/null 2>&1; then
    timeout --kill-after=5s "$seconds" "$@"
  else
    "$@"
  fi
}

if [[ ! -f "$COMPOSE_FILE" ]]; then
  echo "Missing Compose file: $COMPOSE_FILE"
  exit 1
fi

if [[ ! -f "$DEPLOY_DIR/.env" ]]; then
  cp "$DEPLOY_DIR/.env.example" "$DEPLOY_DIR/.env"
  echo "Created $DEPLOY_DIR/.env"
  echo "Review passwords and service addresses in .env, then run this command again."
  exit 1
fi

if grep -Eq '^[A-Z0-9_]+=(change-me)?$' "$DEPLOY_DIR/.env"; then
  echo "Replace all empty or change-me values in $DEPLOY_DIR/.env before deployment."
  exit 1
fi

for service in "${LOG_SERVICES[@]}"; do
  mkdir -p "$DEPLOY_DIR/logs/$service"
  chmod 0777 "$DEPLOY_DIR/logs/$service"
done

echo "[1/4] Checking Docker daemon..."
if ! run_with_timeout 15 docker info; then
  echo "Docker daemon is unavailable or did not respond within 15 seconds."
  echo "Docker service status:"
  systemctl status docker --no-pager -l 2>&1 || true
  echo "Recent Docker service logs:"
  journalctl -u docker -n 50 --no-pager 2>&1 || true
  exit 1
fi

echo "[2/4] Checking Docker Compose..."
if ! run_with_timeout 10 docker compose version; then
  echo "Docker Compose v2 is required on this server."
  exit 1
fi

case "$ACTION" in
  up)
    echo "[3/4] Validating deployment configuration..."
    docker compose -f "$COMPOSE_FILE" config --quiet
    echo "[4/4] Building images with verbose progress output..."
    BUILDKIT_PROGRESS=plain docker compose -f "$COMPOSE_FILE" build
    echo "Image build completed. Starting services..."
    docker compose -f "$COMPOSE_FILE" up -d --no-build
    echo "Services started. Current status:"
    docker compose -f "$COMPOSE_FILE" ps
    ;;
  down)
    docker compose -f "$COMPOSE_FILE" down
    ;;
  restart)
    docker compose -f "$COMPOSE_FILE" restart
    ;;
  logs)
    docker compose -f "$COMPOSE_FILE" logs -f --tail=200
    ;;
  ps)
    docker compose -f "$COMPOSE_FILE" ps
    ;;
  *)
    echo "Usage: $0 {up|down|restart|logs|ps}"
    exit 2
    ;;
esac
