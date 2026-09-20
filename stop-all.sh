#!/bin/sh

set -eu

DEPLOY_DIR="$(cd "$(dirname "$0")" && pwd)"
COMPOSE_FILE="$DEPLOY_DIR/docker-compose.yml"

if [ ! -f "$COMPOSE_FILE" ]; then
  echo "Missing Compose file: $COMPOSE_FILE"
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "Docker daemon is unavailable."
  exit 1
fi

if docker compose version >/dev/null 2>&1; then
  USE_DOCKER_COMPOSE_PLUGIN=true
elif command -v docker-compose >/dev/null 2>&1; then
  USE_DOCKER_COMPOSE_PLUGIN=false
else
  echo "Docker Compose is not installed."
  exit 1
fi

echo "Stopping all crypto-exchange Docker services..."
if [ "$USE_DOCKER_COMPOSE_PLUGIN" = true ]; then
  docker compose -f "$COMPOSE_FILE" stop
else
  docker-compose -f "$COMPOSE_FILE" stop
fi

echo "All crypto-exchange services have been stopped."
echo "Containers, networks, volumes, images, and other Docker projects were not removed."
