#!/bin/bash

# 定义变量
PROJECT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"  # 自动获取项目的根目录
RUN_PATH="$PROJECT_DIR/run"
JAR_NAME="FunASR-1.0-SNAPSHOT.jar"
JAR_PATH="$RUN_PATH/$JAR_NAME"  # 目标目录下的JAR文件路径
MVN_GOALS="clean package"  # Maven 执行的目标，默认是 clean 和 package

# 如果JAR文件不存在，则进行Maven构建和部署
if [ ! -f "$JAR_PATH" ]; then

    echo "开始构建项目..."

    # 进入项目目录
    cd "$PROJECT_DIR" || { echo "进入项目目录失败"; exit 1; }

    # 执行Maven命令
    mvn $MVN_GOALS -DskipTests  # 可以根据需要添加其他Maven参数，例如跳过测试
    if [ $? -ne 0 ]; then
        echo "Maven 构建失败"
        exit 1
    fi

    # 查找生成的JAR文件
    JAR_FILE=$(find "$PROJECT_DIR/target" -name "*.jar" | head -n 1)
    if [ -z "$JAR_FILE" ]; then
        echo "未找到JAR文件"
        exit 1
    fi

    # 创建目标目录（如果不存在）
    mkdir -p "$RUN_PATH"

    # 复制JAR文件到目标目录
    cp "$JAR_FILE" "$JAR_PATH"

    # 检查复制是否成功
    if [ $? -eq 0 ]; then
        echo "部署成功: $JAR_FILE -> $JAR_PATH"
    else
        echo "部署失败"
        exit 1
    fi
fi

