#!/bin/bash

# 定义变量
PROJECT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"  # 自动获取项目的根目录
MODELS_DIR="$PROJECT_DIR/models"
FUNASR_IMAGE="registry.cn-hangzhou.aliyuncs.com/funasr_repo/funasr:funasr-runtime-sdk-cpu-0.4.6"
CONTAINER_NAME="funasr_container"
PORT="10095"
START_SERVICE_SCRIPT="start_funasr_service.sh"

# 创建模型存储目录（如果不存在）
mkdir -p "$MODELS_DIR"

# 复制启动服务脚本到模型目录，并强制覆盖已存在的文件
cp -f "$START_SERVICE_SCRIPT" "$MODELS_DIR/"

# 拉取Docker镜像（如果本地没有该镜像）
if ! docker image inspect "$FUNASR_IMAGE" &> /dev/null; then
  echo "Pulling Docker image..."
  docker pull "$FUNASR_IMAGE"
else
  echo "Docker image already exists."
fi

# 检查是否存在同名容器并处理
if docker ps -a --format '{{.Names}}' | grep -wq "$CONTAINER_NAME"; then
  # 检查容器是否正在运行
  if docker ps --format '{{.Names}}' | grep -wq "$CONTAINER_NAME"; then
    echo "Container $CONTAINER_NAME is already running. Attaching to the container..."
    docker exec -it "$CONTAINER_NAME" /bin/bash
    exit 0  # 退出脚本，不继续启动新容器
  else
    echo "A container with name $CONTAINER_NAME already exists but is not running. Starting the existing container and executing start script..."
    docker start "$CONTAINER_NAME"
    # 等待容器完全启动
    sleep 5
    # 更新容器内启动脚本路径并执行
    docker exec -it "$CONTAINER_NAME" /bin/bash -c "chmod +x /workspace/models/$START_SERVICE_SCRIPT && /workspace/models/$START_SERVICE_SCRIPT"
    exit 0  # 退出脚本，不继续启动新容器
  fi
fi

# 运行Docker容器并附加启动服务脚本
echo "Running Docker container..."
docker run -d -p "$PORT:$PORT" --privileged=true \
  -v "$MODELS_DIR":/workspace/models \
  --name "$CONTAINER_NAME" \
  "$FUNASR_IMAGE"

# 等待容器完全启动
sleep 5

# 在新启动的容器中执行启动脚本
docker exec -it "$CONTAINER_NAME" /bin/bash -c "chmod +x /workspace/models/$START_SERVICE_SCRIPT && /workspace/models/$START_SERVICE_SCRIPT"
