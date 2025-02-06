import cv2
import os
import numpy as np
import argparse

def extract_frames(video_path, output_folder, threshold=10.0):
    # 打开视频文件
    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        print(f"无法打开视频文件: {video_path}")
        return

    # 获取视频文件名（不含扩展名）
    video_name = os.path.splitext(os.path.basename(video_path))[0]
    # 创建保存图片的文件夹
    output_folder = os.path.join(output_folder, f"output_{video_name}")
    os.makedirs(output_folder, exist_ok=True)
    print(f"创建文件夹: {output_folder}")

    # 获取视频的帧率（FPS）
    fps = cap.get(cv2.CAP_PROP_FPS)
    frames_per_second = int(round(fps))  # 每秒的帧数
    print(f"视频帧率 (FPS): {fps}")

    prev_frame = None
    frame_count = 0
    second_count = 0

    while True:
        ret, curr_frame = cap.read()
        if not ret:
            break  # 视频结束

        # 每隔 1 秒处理一次
        if frame_count % frames_per_second == 0:
            if prev_frame is not None:
                # 计算帧间差异
                diff = cv2.absdiff(prev_frame, curr_frame)
                mean_diff = np.mean(diff)

                # 如果差异超过阈值，保存当前帧
                if mean_diff > threshold:
                    # 获取当前时间戳（秒）
                    total_seconds = second_count
                    hours = total_seconds // 3600
                    minutes = (total_seconds % 3600) // 60
                    seconds = total_seconds % 60

                    # 生成时分秒格式的文件名
                    time_stamp = f"{hours:02d}_{minutes:02d}_{seconds:02d}"
                    output_path = os.path.join(output_folder, f"frame_{time_stamp}.jpg")

                    # 保存不一致的帧
                    cv2.imwrite(output_path, curr_frame)
                    print(f"保存不一致帧: {output_path}")

            # 更新前一帧
            prev_frame = curr_frame.copy()
            second_count += 1

        frame_count += 1

    # 释放资源
    cap.release()
    print(f"视频处理完成: {video_path}")
    print("------------------------------")


def process_videos_in_directory(video_dir, output_base_folder, threshold=10.0):
    # 遍历视频目录中的所有文件
    for root, dirs, files in os.walk(video_dir):
        for file in files:
            # 检查文件是否是视频文件（根据扩展名）
            if file.lower().endswith(('.mp4', '.avi', '.mov', '.mkv', '.flv')):
                video_path = os.path.join(root, file)
                print(f"开始处理视频: {video_path}")
                extract_frames(video_path, output_base_folder, threshold)


def main():
    # 设置命令行参数解析
    parser = argparse.ArgumentParser(description="批量处理视频文件，提取有差异的帧")
    parser.add_argument("video_dir", type=str, help="视频文件目录路径")
    parser.add_argument("--output", type=str, default="../run/outFrames", help="输出文件夹路径")
    parser.add_argument("--threshold", type=float, default=0.5, help="帧间差异阈值")
    args = parser.parse_args()

    # 创建输出文件夹
    os.makedirs(args.output, exist_ok=True)

    # 批量处理视频文件
    process_videos_in_directory(args.video_dir, args.output, args.threshold)

# 安装依赖 pip install -r requirements.txt
if __name__ == "__main__":
    main()