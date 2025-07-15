#!/bin/bash
echo "install phase"
# 앱 프로세스 종료
APP_NAME="app.jar"
APP_DIR="/home/ubuntu/app"
PID=$(pgrep -f $APP_NAME)
if [ -n "$PID" ]; then
  echo "Stopping existing application (PID: $PID)"
  kill -9 "$PID"
else
  echo "No application to stop"
fi
echo "Fixing permissions on app directory"
sudo mkdir -p /home/ubuntu/app
sudo chown -R ubuntu:ubuntu /home/ubuntu/app
sudo chmod -R 750 /home/ubuntu/app

echo "Downloading CloudWatch Agent deb package"
wget https://s3.ap-northeast-2.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb -O /tmp/amazon-cloudwatch-agent.deb

echo "Installing CloudWatch Agent"
sudo dpkg -i /tmp/amazon-cloudwatch-agent.deb || sudo apt-get -f install -y

# 앱 디렉토리 생성
mkdir -p $APP_DIR
