#!/bin/bash

set -e  # 에러 발생 시 즉시 종료

echo "🚀 [START] Docker 환경 설정 및 서비스 기동 시작..."

# 로그 디렉토리 생성
echo "📂 logs 디렉토리 생성..."
cd /home/ubuntu
mkdir -p logs

# Docker 및 Compose 플러그인 설치 (Docker 공식 저장소 사용)
echo "📦 Docker 및 Compose 플러그인 설치 (공식 저장소 기준 noble/jammy)..."
sudo apt update
sudo apt install -y apt-transport-https ca-certificates curl gnupg lsb-release

sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Ubuntu 코드네임을 자동으로 가져와도 되지만, noble이 아직 안정화된 저장소가 아닐 수 있으므로
# 특히 docker-compose-plugin 설치에 실패할 경우 jammy로 강제할 수 있습니다.
CODENAME=$(lsb_release -cs)
if [[ "$CODENAME" == "noble" ]]; then
  REPO_CODENAME="noble"
else
  REPO_CODENAME="$CODENAME"
fi

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $REPO_CODENAME stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

echo "✅ Docker 설치 완료: 코드네임 '$REPO_CODENAME' 기준으로 설치됨"

# Docker 데몬 활성화 및 시작
echo "🔧 Docker 서비스 시작 중..."
sudo systemctl enable docker
sudo systemctl start docker

# Docker 그룹 권한 추가
echo "👤 ubuntu 사용자에게 docker 그룹 권한 부여..."
sudo groupadd docker || true
sudo usermod -aG docker ubuntu

# Docker 소켓 권한 확인 및 재시작
sudo systemctl restart docker

# 새로운 그룹 적용 세션 실행
echo "✨ docker 그룹 권한 적용세션 시작..."
exec sg docker "$0"
# 또는: exec newgrp docker  # 단, 이 이후 스크립트가 재실행됩니다.

# Compose 실행
cd /home/ubuntu
sudo docker compose down || true
sudo docker compose up -d

echo "✅ [DONE] 서비스 및 Docker Compose 실행 완료!"
