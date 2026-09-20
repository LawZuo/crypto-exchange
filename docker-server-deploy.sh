#!/usr/bin/env bash

set -Eeuo pipefail

DEPLOY_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$DEPLOY_DIR/docker-compose.yml"
ENV_FILE="$DEPLOY_DIR/.env"
ACTION="${1:-up}"

if [[ $# -gt 0 ]]; then
  shift
fi

SERVICES=(
  exchange-gateway
  exchange-web
  exchange-admin
  exchange-auth
  exchange-business-user
  exchange-business-account
  exchange-business-market
  exchange-business-spot
  exchange-datasource
  exchange-resource
)

JARS=(
  exchange-gateway.jar
  exchange-web.jar
  exchange-admin.jar
  exchange-auth.jar
  exchange-business-user.jar
  exchange-business-account.jar
  exchange-business-market.jar
  exchange-business-spot.jar
  exchange-datasource.jar
  exchange-resource.jar
)

usage() {
  cat <<'EOF'
Usage: ./deploy.sh <command> [service...]

Commands:
  up [service...]       Build and start all or selected services
  build [service...]    Build all or selected service images
  start [service...]    Start existing containers
  stop [service...]     Stop containers without removing them
  restart [service...]  Restart all or selected containers
  down                  Stop and remove this project's containers and network
  logs [service...]     Follow logs (all services when omitted)
  ps                    Show service status
  config                Validate and print the resolved Compose configuration
EOF
}

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

run_with_timeout() {
  local seconds="$1"
  shift
  if command -v timeout >/dev/null 2>&1; then
    timeout --kill-after=5s "$seconds" "$@"
  else
    "$@"
  fi
}

require_file() {
  [[ -f "$1" ]] || fail "Missing file: $1"
}

prepare_env() {
  if [[ ! -f "$ENV_FILE" ]]; then
    if [[ "$ACTION" == "up" || "$ACTION" == "build" ]]; then
      require_file "$DEPLOY_DIR/.env.example"
      install -m 0600 "$DEPLOY_DIR/.env.example" "$ENV_FILE"
      echo "Created $ENV_FILE"
      fail "Review .env values, then run the command again."
    fi
    fail "Missing environment file: $ENV_FILE"
  fi

  if grep -Eq '^[A-Z0-9_.]+[[:space:]]*=[[:space:]]*(|change-me)[[:space:]]*$' "$ENV_FILE"; then
    fail "Replace all empty or change-me values in $ENV_FILE."
  fi
}

prepare_log_directories() {
  local service
  for service in "${SERVICES[@]}"; do
    install -d -m 0777 "$DEPLOY_DIR/logs/$service"
    # 镜像使用非 root 的 exchange 用户（当前 UID/GID 999）。发布脚本通常由
    # root 执行，挂载目录若保持 root:root 775，会导致 Logback 无法创建文件，
    # 应用随即进入重启循环。这里同时兼容已存在的目录和日志文件。
    chmod -R a+rwX "$DEPLOY_DIR/logs/$service"
  done
}

check_jars() {
  local jar
  for jar in "${JARS[@]}"; do
    require_file "$DEPLOY_DIR/jars/$jar"
  done
}

show_failed_services() {
  local failed_services
  failed_services="$("${COMPOSE[@]}" ps --all --status exited --services 2>/dev/null || true)"
  if [[ -n "$failed_services" ]]; then
    echo "The following services exited during startup:" >&2
    echo "$failed_services" >&2
    # shellcheck disable=SC2086
    "${COMPOSE[@]}" logs --tail=100 $failed_services >&2 || true
    return 1
  fi
}

case "$ACTION" in
  up|build|start|stop|restart|down|logs|ps|config) ;;
  -h|--help|help)
    usage
    exit 0
    ;;
  *)
    usage >&2
    exit 2
    ;;
esac

require_file "$COMPOSE_FILE"

echo "[1/3] Checking Docker daemon..."
if ! run_with_timeout 15 docker info >/dev/null; then
  fail "Docker daemon is unavailable or did not respond within 15 seconds."
fi

echo "[2/3] Checking Docker Compose..."
if ! run_with_timeout 10 docker compose version >/dev/null; then
  fail "Docker Compose v2 is required."
fi

prepare_env
COMPOSE=(docker compose --project-directory "$DEPLOY_DIR" --env-file "$ENV_FILE" -f "$COMPOSE_FILE")

echo "[3/3] Running '$ACTION' for crypto-exchange..."
case "$ACTION" in
  up)
    check_jars
    prepare_log_directories
    "${COMPOSE[@]}" config --quiet
    BUILDKIT_PROGRESS=plain "${COMPOSE[@]}" build "$@"
    "${COMPOSE[@]}" up -d --no-build "$@"
    "${COMPOSE[@]}" ps --all
    show_failed_services
    ;;
  build)
    check_jars
    "${COMPOSE[@]}" config --quiet
    BUILDKIT_PROGRESS=plain "${COMPOSE[@]}" build "$@"
    ;;
  start)
    prepare_log_directories
    "${COMPOSE[@]}" start "$@"
    "${COMPOSE[@]}" ps --all
    ;;
  stop)
    "${COMPOSE[@]}" stop "$@"
    ;;
  restart)
    "${COMPOSE[@]}" restart "$@"
    "${COMPOSE[@]}" ps --all
    ;;
  down)
    [[ $# -eq 0 ]] || fail "The down command does not accept service names. Use stop for selected services."
    "${COMPOSE[@]}" down
    ;;
  logs)
    "${COMPOSE[@]}" logs --follow --tail=200 "$@"
    ;;
  ps)
    [[ $# -eq 0 ]] || fail "The ps command does not accept service names."
    "${COMPOSE[@]}" ps --all
    ;;
  config)
    [[ $# -eq 0 ]] || fail "The config command does not accept service names."
    "${COMPOSE[@]}" config
    ;;
esac
