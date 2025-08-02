#!/usr/bin/env bash

set -euo pipefail

WORK_DIR="/home/ubuntu"
COMPOSE_FILE="docker-compose.yml"     # 필요 시 경로 수정

echo "🚀 Starting Promtail via docker compose …"
cd "$WORK_DIR"

sudo chmod 666 /var/run/docker.sock

docker compose -f "$COMPOSE_FILE" down --remove-orphans || true
docker compose -f "$COMPOSE_FILE" pull
docker compose -f "$COMPOSE_FILE" up -d --remove-orphans

echo "✅ Promtail containers running."
