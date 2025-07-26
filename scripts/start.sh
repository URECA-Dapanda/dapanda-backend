#!/bin/bash
echo "==== APPLICATION START ===="

APP_NAME="app.jar"
APP_DIR="/home/ubuntu/app"
LOG_DIR="/var/log/myapp"
LOG_FILE="$LOG_DIR/app.log"
CLOUDWATCH_AGENT_BIN="/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl"

# 로그 디렉토리 생성
sudo mkdir -p "$LOG_DIR"
sudo chown ubuntu:ubuntu "$LOG_DIR"

# 앱 실행
echo "Starting $APP_NAME with prod profile..."
nohup java -jar "$APP_DIR/$APP_NAME" --spring.profiles.active=prod > "$LOG_FILE" 2>&1 &

# === 앱 Health Check 대기 ===
echo "Waiting for application health check to pass..."

for i in {1..180}; do
  sleep 2
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
  echo "  [$i] Health check status: $STATUS"

  if [ "$STATUS" -eq 200 ]; then
    echo "✅ Application is healthy."
    break
  elif [ "$STATUS" -eq 000 ]; then
    echo "  [$i] Service not reachable yet..."
  fi
done

if [ "$STATUS" -ne 200 ]; then
  echo "❌ Application health check failed. Showing last 20 lines of log:"
  tail -n 20 "$LOG_FILE"
  exit 1
fi

# ===== CloudWatch Agent 설치 여부 확인 후 설치 =====
echo "Checking CloudWatch Agent installation..."
if ! command -v "$CLOUDWATCH_AGENT_BIN" &> /dev/null; then
  echo "CloudWatch Agent not found. Installing..."
  sudo apt update -y
  sudo apt install -y amazon-cloudwatch-agent
else
  echo "CloudWatch Agent is already installed."
fi

# ===== CloudWatch 설정 파일 생성 =====
CLOUDWATCH_CONFIG="/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json"
echo "Generating CloudWatch Agent configuration..."
sudo mkdir -p "$(dirname "$CLOUDWATCH_CONFIG")"

cat <<EOF | sudo tee "$CLOUDWATCH_CONFIG"
{
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "$LOG_FILE",
            "log_group_name": "MyAppLogs",
            "log_stream_name": "{instance_id}",
            "timestamp_format": "%Y-%m-%d %H:%M:%S"
          }
        ]
      }
    }
  }
}
EOF

# ===== CloudWatch Agent 시작 =====
echo "Starting CloudWatch Agent..."
sudo "$CLOUDWATCH_AGENT_BIN" \
  -a fetch-config \
  -m ec2 \
  -c file:"$CLOUDWATCH_CONFIG" \
  -s

echo "🚀 Application and CloudWatch Agent started successfully."
