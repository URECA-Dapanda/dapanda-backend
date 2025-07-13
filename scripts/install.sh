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

# 앱 디렉토리 생성
mkdir -p $APP_DIR
