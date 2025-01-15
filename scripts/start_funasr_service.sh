#!/bin/bash

# 定义变量
FUNASR_RUNTIME_DIR="/workspace/FunASR/runtime"
MODELS_DIR="/workspace/models"
VAD_DIR="damo/speech_fsmn_vad_zh-cn-16k-common-onnx"
MODEL_DIR="damo/speech_paraformer-large-vad-punc_asr_nat-zh-cn-16k-common-vocab8404-onnx"
PUNC_DIR="damo/punc_ct-transformer_cn-en-common-vocab471067-large-onnx"
LM_DIR="damo/speech_ngram_lm_zh-cn-ai-wesp-fst"
ITN_DIR="thuduj12/fst_itn_zh"
HOTWORD_FILE="$MODELS_DIR/hotwords.txt"
LOG_FILE="$FUNASR_RUNTIME_DIR/log.txt"

# 进入 FunASR 运行目录
cd "$FUNASR_RUNTIME_DIR" || { echo "Directory $FUNASR_RUNTIME_DIR not found!"; exit 1; }

# 启动 funasr-wss-server 服务程序
echo "Starting funasr-wss-server service..."
nohup bash run_server.sh \
  --keyfile 0 \
  --certfile 0 \
  --download-model-dir "$MODELS_DIR" \
  --vad-dir "$VAD_DIR" \
  --model-dir "$MODEL_DIR" \
  --punc-dir "$PUNC_DIR" \
  --lm-dir "$LM_DIR" \
  --itn-dir "$ITN_DIR" \
  --hotword "$HOTWORD_FILE" > "$LOG_FILE" 2>&1 &

# 等待一段时间确保服务已启动（可根据需要调整等待时间）
sleep 5

# 实时查看日志文件的最后50行
echo "Tailing the last 50 lines of the log file at $LOG_FILE..."
tail -n 50 -f "$LOG_FILE"
