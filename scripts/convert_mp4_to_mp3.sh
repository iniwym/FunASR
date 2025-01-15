#!/bin/bash

# 检查ffmpeg是否安装
if ! command -v ffmpeg &> /dev/null; then
  echo "ffmpeg未安装，请先安装ffmpeg。"
  exit 1
fi

# 检查是否提供了足够的参数
if [ "$#" -ne 2 ]; then
  echo "使用方法: $0 <输入文件夹路径> <输出文件夹路径>"
  exit 1
fi

# 获取命令行参数
INPUT_FOLDER_PATH=$1
OUTPUT_FOLDER_PATH=$2

# 创建输出文件夹，如果不存在的话
mkdir -p "$OUTPUT_FOLDER_PATH"

# 初始化一个标志变量，用于检测是否找到了MP4文件
found_mp4=false

# 遍历文件夹内的所有MP4文件
for mp4_file in "$INPUT_FOLDER_PATH"/*.mp4
do
  # 检查是否存在符合条件的文件
  if [ -f "$mp4_file" ]; then
    found_mp4=true
    # 获取文件名，不带扩展名
    file_name=$(basename "$mp4_file" .mp4)
    # 定义输出MP3文件路径
    mp3_file="$OUTPUT_FOLDER_PATH/$file_name.mp3"
    # 使用ffmpeg进行转换
    echo "正在转换 $mp4_file 到 $mp3_file..."
    ffmpeg -i "$mp4_file" -q:a 0 -map a "$mp3_file"
    
    if [ $? -eq 0 ]; then
      echo "转换 $mp4_file 完成！"
    else
      echo "转换 $mp4_file 失败。"
    fi
  fi
done

# 如果没有找到任何MP4文件，输出提示信息
if [ "$found_mp4" = false ]; then
  echo "在输入文件夹中没有找到MP4文件。"
fi

echo "所有文件转换完成（如果存在的话）。"

