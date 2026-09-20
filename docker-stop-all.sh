#!/usr/bin/env bash

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
COMPOSE_FILE="$PROJECT_DIR/docker-compose.yml"

if [[ ! -f "$COMPOSE_FILE" ]]; then
  echo "Missing Compose file: $COMPOSE_FILE"
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "Docker daemon is unavailable."
  exit 1
fi

if docker compose version >/dev/null 2>&1; then
  COMPOSE_COMMAND=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_COMMAND=(docker-compose)
else
  echo "Docker Compose is not installed."
  exit 1
fi

echo "Stopping all crypto-exchange Docker services..."
"${COMPOSE_COMMAND[@]}" -f "$COMPOSE_FILE" stop

echo "All crypto-exchange services have been stopped."
echo "Containers, networks, volumes, images, and other Docker projects were not removed."
