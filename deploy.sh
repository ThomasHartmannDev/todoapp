#!/usr/bin/env bash
set -euo pipefail

IMAGE_NAME="${IMAGE_NAME:-todoapp:latest}"
CONTAINER_NAME="${CONTAINER_NAME:-todoapp}"
HOST_PORT="${HOST_PORT:-8080}"
CONTAINER_PORT="${CONTAINER_PORT:-8080}"
ENV_FILE="${ENV_FILE:-.env}"

docker build -t "$IMAGE_NAME" .

if docker ps -a --format '{{.Names}}' | grep -Fxq "$CONTAINER_NAME"; then
  docker rm -f "$CONTAINER_NAME"
fi

run_args=(
  --detach
  --name "$CONTAINER_NAME"
  --publish "$HOST_PORT:$CONTAINER_PORT"
)

if [ -f "$ENV_FILE" ]; then
  run_args+=(--env-file "$ENV_FILE")
fi

docker run "${run_args[@]}" "$IMAGE_NAME"

echo "TodoApp is running at http://localhost:${HOST_PORT}"
