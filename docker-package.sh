#!/usr/bin/env bash

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
RELEASE_DIR="$PROJECT_DIR/dist"
BUNDLE_NAME="crypto-exchange-docker"
BUNDLE_DIR="$RELEASE_DIR/$BUNDLE_NAME"

resolve_maven() {
  if [[ -n "${MAVEN_CMD:-}" ]]; then
    [[ -x "$MAVEN_CMD" ]] || {
      echo "MAVEN_CMD is not executable: $MAVEN_CMD" >&2
      return 1
    }
    printf '%s\n' "$MAVEN_CMD"
    return
  fi

  if command -v mvn >/dev/null 2>&1; then
    command -v mvn
    return
  fi

  if [[ -x "$PROJECT_DIR/mvnw" ]]; then
    printf '%s\n' "$PROJECT_DIR/mvnw"
    return
  fi

  local user_name candidate
  user_name="$(id -un)"
  shopt -s nullglob
  for candidate in \
    "/Users/$user_name"/.m2/wrapper/dists/*/*/apache-maven-*/bin/mvn \
    "/home/$user_name"/.m2/wrapper/dists/*/*/apache-maven-*/bin/mvn; do
    if [[ -x "$candidate" ]]; then
      printf '%s\n' "$candidate"
      shopt -u nullglob
      return
    fi
  done
  shopt -u nullglob

  echo "Maven was not found. Install Maven, add mvn to PATH, or set MAVEN_CMD." >&2
  return 1
}

if [[ "${1:-}" == "--skip-build" ]]; then
  echo "[1/2] Reusing existing Spring Boot packages..."
else
  echo "[1/2] Packaging Spring Boot services..."
  MAVEN_EXECUTABLE="$(resolve_maven)"
  echo "Using Maven: $MAVEN_EXECUTABLE"
  if [[ "${MAVEN_OFFLINE:-false}" == "true" ]]; then
    "$MAVEN_EXECUTABLE" -o -f "$PROJECT_DIR/exchange-parent/pom.xml" clean package -Dmaven.test.skip=true
  else
    "$MAVEN_EXECUTABLE" -f "$PROJECT_DIR/exchange-parent/pom.xml" clean package -Dmaven.test.skip=true
  fi
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
install -m 0644 "$PROJECT_DIR/exchange-business/exchange-business-spot/target/exchange-business-spot-1.0.jar" \
  "$BUNDLE_DIR/jars/exchange-business-spot.jar"
install -m 0644 "$PROJECT_DIR/exchange-module/exchange-module-datasource/target/exchange-module-datasource-1.0.jar" \
  "$BUNDLE_DIR/jars/exchange-datasource.jar"
install -m 0644 "$PROJECT_DIR/exchange-module/exchange-module-resource/target/exchange-module-resource-1.0.jar" \
  "$BUNDLE_DIR/jars/exchange-resource.jar"

install -m 0644 "$PROJECT_DIR/Dockerfile" "$BUNDLE_DIR/Dockerfile"
install -m 0644 "$PROJECT_DIR/docker-compose.yml" "$BUNDLE_DIR/docker-compose.yml"
install -m 0644 "$PROJECT_DIR/.dockerignore" "$BUNDLE_DIR/.dockerignore"
install -m 0644 "$PROJECT_DIR/.env.example" "$BUNDLE_DIR/.env.example"
install -m 0755 "$PROJECT_DIR/docker-server-deploy.sh" "$BUNDLE_DIR/deploy.sh"
install -m 0755 "$PROJECT_DIR/stop-all.sh" "$BUNDLE_DIR/stop-all.sh"

echo "Docker release directory created: $BUNDLE_DIR"
