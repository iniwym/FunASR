#!/bin/bash

# 检查是否提供了输入目录参数
if [ -z "$1" ]; then
  echo "使用方法: $0 输入目录"
  exit 1
fi

# 输入目录
input_dir="$1"

# 检查输入目录是否存在
if [ ! -d "$input_dir" ]; then
  echo "错误: 目录 '$input_dir' 不存在。"
  exit 1
fi

# 创建输出目录（输入目录名后加上 _split）
output_dir="${input_dir}_split"
mkdir -p "$output_dir"

# 遍历输入目录下的所有mp3文件
for file in "$input_dir"/*.mp3; do
  if [ -e "$file" ]; then # 确认文件存在
    filename=$(basename -- "$file")
    filename_no_ext="${filename%.*}"

    # 使用ffmpeg分割音频文件，并将结果输出到输出目录
    ffmpeg -i "$file" -f segment -segment_time 3600 -c copy "$output_dir/${filename_no_ext}_%03d.mp3"
  else
    echo "未找到任何MP3文件。"
  fi
done

echo "处理完成。所有分割后的文件已保存到 '$output_dir'。"

open "$output_dir"