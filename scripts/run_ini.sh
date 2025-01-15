#!/bin/bash

# 定义变量
PROJECT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"  # 自动获取项目的根目录
SCRIPTS_PATH="$PROJECT_DIR/scripts"
RUN_PATH="$PROJECT_DIR/run"
JAR_NAME="FunASR-1.0-SNAPSHOT.jar"

while true; do

	# 显示菜单
	echo "------------------------------"
	echo "请选择要执行的脚本："
	echo "0. 退出程序"
	echo "1. 视频转音频"
	echo "2. 启动Funasr服务"
	echo "3. 音频转文字"
	echo "4. 关闭Funasr服务"

	# 读取用户选择的脚本
	read -p "请输入选项 (0/1/2/3/4): " choice

	# 根据选择执行相应的脚本并提示输入参数
	case $choice in
		0)
			echo "退出程序~"
			break;;
		1)
			read -p "请输入 输入目录: " input_dir_1
			read -p "请输入 输出目录: " output_dir_1
			sh "$SCRIPTS_PATH/convert_mp4_to_mp3.sh" "$input_dir_1" "$output_dir_1"
			;;
		2)
			echo "在新终端操作查看~"
			osascript -e "tell application \"Terminal\"" \
			-e "activate" \
			-e "do script \"cd '$SCRIPTS_PATH'; sh run_funasr_container.sh; exec \$SHELL\"" \
			-e "end tell"	
			;;
		3)
			read -p "请输入 输入目录: " input_dir_3
			sh "$SCRIPTS_PATH/deploy_funasr_java.sh"
			cd "$PROJECT_DIR"
			java -jar "$RUN_PATH/$JAR_NAME" "$input_dir_3"
			open "$RUN_PATH/outTxt"
			;;
		4)
			echo "关闭Funasr服务中~"
			sh "$SCRIPTS_PATH/stop_funasr_container.sh"
			;;	
		*)
			echo "无效的选项~"
			;;
	esac
done

echo "程序已结束~"
