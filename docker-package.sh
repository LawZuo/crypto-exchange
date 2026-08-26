#!/usr/bin/env bash

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
RELEASE_DIR="$PROJECT_DIR/dist"
BUNDLE_NAME="crypto-exchange-docker"
BUNDLE_DIR="$RELEASE_DIR/$BUNDLE_NAME"

if [[ "${1:-}" == "--skip-build" ]]; then
  echo "[1/2] Reusing existing Spring Boot packages..."
else
  echo "[1/2] Packaging Spring Boot services..."
  MAVEN_FLAGS=()
  if [[ "${MAVEN_OFFLINE:-false}" == "true" ]]; then
    MAVEN_FLAGS+=("-o")
  fi
  mvn "${MAVEN_FLAGS[@]}" -f "$PROJECT_DIR/exchange-parent/pom.xml" clean package -Dmaven.test.skip=true
fi

if [[ "$BUNDLE_DIR" != "$PROJECT_DIR/dist/crypto-exchange-docker" ]]; then
  echo "Unexpected release directory: $BUNDLE_DIR"
  exit 1
fi

echo "[2/2] Preparing Docker release directory..."
rm -rf "$BUNDLE_DIR"
mkdir -p "$BUNDLE_DIR/jars"

install -m 0644 "$PROJECT_DIR/exchange-gateway/target/exchange-gateway-1.0.jar" \
  "$BUNDLE_DIR/jars/exchange-gateway.jar"
install -m 0644 "$PROJECT_DIR/exchange-web/target/exchange-web-1.0.jar" \
  "$BUNDLE_DIR/jars/exchange-web.jar"
install -m 0644 "$PROJECT_DIR/exchange-admin/target/exchange-admin-1.0.jar" \
  "$BUNDLE_DIR/jars/exchange-admin.jar"
install -m 0644 "$PROJECT_DIR/exchange-auth/target/exchange-auth-1.0.jar" \
  "$BUNDLE_DIR/jars/exchange-auth.jar"
install -m 0644 "$PROJECT_DIR/exchange-business/exchange-business-user/target/exchange-business-user-1.0.jar" \
  "$BUNDLE_DIR/jars/exchange-business-user.jar"
install -m 0644 "$PROJECT_DIR/exchange-business/exchange-bussiness-account/target/exchange-bussiness-account-1.0.jar" \
  "$BUNDLE_DIR/jars/exchange-business-account.jar"
install -m 0644 "$PROJECT_DIR/exchange-business/exchange-business-market/target/exchange-business-market-1.0.jar" \
  "$BUNDLE_DIR/jars/exchange-business-market.jar"
install -m 0644 "$PROJECT_DIR/exchange-module/exchange-module-datasource/target/exchange-module-datasource-1.0.jar" \
  "$BUNDLE_DIR/jars/exchange-datasource.jar"
install -m 0644 "$PROJECT_DIR/exchange-module/exchange-module-resource/target/exchange-module-resource-1.0.jar" \
  "$BUNDLE_DIR/jars/exchange-resource.jar"

install -m 0644 "$PROJECT_DIR/Dockerfile" "$BUNDLE_DIR/Dockerfile"
install -m 0644 "$PROJECT_DIR/docker-compose.yml" "$BUNDLE_DIR/docker-compose.yml"
install -m 0644 "$PROJECT_DIR/.dockerignore" "$BUNDLE_DIR/.dockerignore"
install -m 0644 "$PROJECT_DIR/.env.example" "$BUNDLE_DIR/.env.example"
install -m 0755 "$PROJECT_DIR/docker-server-deploy.sh" "$BUNDLE_DIR/deploy.sh"

echo "Docker release directory created: $BUNDLE_DIR"
