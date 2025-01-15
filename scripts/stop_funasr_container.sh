#!/bin/bash

# 定义变量
CONTAINER_NAME="funasr_container"

# 检查是否存在指定名称的容器
if docker ps -a --format '{{.Names}}' | grep -wq "$CONTAINER_NAME"; then
  # 检查容器是否正在运行
  if docker ps --format '{{.Names}}' | grep -wq "$CONTAINER_NAME"; then
    echo "Stopping running container $CONTAINER_NAME..."
    docker stop "$CONTAINER_NAME"
  else
    echo "Container $CONTAINER_NAME is already stopped."
  fi
else
  echo "No container found with name $CONTAINER_NAME."
fi
