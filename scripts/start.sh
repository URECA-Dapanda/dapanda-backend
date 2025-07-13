#!/bin/bash

echo "==== APPLICATION START ===="

APP_NAME="app.jar"
APP_DIR="/home/ubuntu/app"
LOG_DIR="/home/ubuntu/app"
LOG_FILE="$LOG_DIR/app.log"

# 로그 디렉토리 생성
mkdir -p "$LOG_DIR"

# 앱 실행
echo "Starting $APP_NAME with prod profile..."
nohup java -jar "$APP_DIR/$APP_NAME" --spring.profiles.active=prod > "$LOG_FILE" 2>&1 &
