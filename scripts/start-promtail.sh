#!/bin/bash

cd /home/ubuntu
mkdir -p logs
docker compose down || true
docker compose up -d
