#!/bin/bash

set -e  # 에러 발생 시 즉시 종료

echo "🚀 [START] Docker 환경 설정 및 서비스 기동 시작..."

# 1. 로그 디렉토리 생성
echo "📂 logs 디렉토리 생성..."
cd /home/ubuntu
mkdir -p logs

# 2. Docker 및 compose-plugin 설치
echo "📦 Docker 및 Compose 플러그인 설치..."
sudo apt update
sudo apt install -y docker.io docker-compose-plugin

# 3. Docker 데몬 활성화 및 시작
echo "🔧 Docker 서비스 시작 중..."
sudo systemctl enable docker
sudo systemctl start docker

# 4. 현재 사용자 docker 그룹에 추가 (이미 추가된 경우 무시)
echo "👤 현재 사용자 docker 그룹에 추가..."
sudo usermod -aG docker $USER || true

# 5. docker compose 실행
echo "📦 Docker Compose 서비스 재시작..."
/usr/bin/docker compose down || true
/usr/bin/docker compose up -d

echo "✅ [DONE] 서비스 및 Docker Compose 실행 완료!"
