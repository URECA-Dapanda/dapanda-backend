#!/usr/bin/env bash
set -euo pipefail

# 1) Docker 미설치 시에만 진행
if ! command -v docker &>/dev/null; then
  echo "🔧 Installing Docker CE & compose-plugin ..."
  apt-get update -y
  # Jammy 저장소 사용 → noble도 호환
  apt-get install -y docker.io docker-compose-plugin
  systemctl enable --now docker
fi

# 2) ubuntu 사용자를 docker 그룹에 추가
if ! id ubuntu | grep -q "(docker)"; then
  usermod -aG docker ubuntu
  echo "➕ added ubuntu to docker group"
fi

echo "✅ Docker ready"
