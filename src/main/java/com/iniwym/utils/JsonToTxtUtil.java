package com.iniwym.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 解析json文件中的stamp_sents字段，并保存为txt文件。
 * @Author: iniwym
 * @Date: 2025-01-13
 */
public class JsonToTxtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonToTxtUtil.class);

    /**
     * 主方法，用于调用处理和保存 stamp_sents 的逻辑。
     */
    public static void main(String[] args) {
        // 设置 JSON 文件路径
        String jsonFilePath = "./test/20250113_1620531.json";
        // 调用处理并保存的方法
        processAndSaveStampSents(jsonFilePath);
    }

    /**
     * 处理 stamp_sents 并将结果保存到指定的输出文件中。
     *
     * @param jsonFilePath JSON 文件路径
     */
    public static void processAndSaveStampSents(String jsonFilePath) {
        // 定义输出文件路径
        String outputFilePath = null;
        try {
            // 读取 JSON 文件内容
            String jsonStr = readJsonFile(jsonFilePath);
            // 解析 JSON 字符串为 JSONObject
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            // 获取 stamp_sents 数组
            JSONArray stampSents = jsonObject.getJSONArray("stamp_sents");
            // 获取 wav_name 字段
            String wav_name = (String) jsonObject.get("wav_name");
            // 生成带有时间戳的输出文件路径
            outputFilePath = generateOutputFilePath(wav_name);

            // 保存 wav_name 到输出文件中
            saveToFile(outputFilePath, ("标题：【" + wav_name + "】\n"));

            StringBuilder accumulatedText = new StringBuilder();
            // 遍历 stamp_sents 数组中的每个元素
            for (int i = 0; i < stampSents.size(); i++) {
                // 获取当前元素
                JSONObject item = stampSents.getJSONObject(i);
                // 调整 text_seg 字段的内容
                String textSeg = adjustTextSeg(item.getString("text_seg"));
                // 获取标点符号
                String punc = item.getString("punc");
                // 获取当前句子的开始时间
                long currentStart = item.getLongValue("start");

                // 如果是句号或问号，则输出累积的文本
                if ("。".equals(punc) || "？".equals(punc)) {
                    accumulatedText.append(textSeg).append(punc);
                    // 将格式化后的时间和累积的文本写入文件
                    saveToFile(outputFilePath, formatTime(currentStart / 1000) + " " + accumulatedText.toString() + "\n");
                    // 清空累积文本
                    accumulatedText.setLength(0);
                } else {
                    // 否则继续累积文本
                    accumulatedText.append(textSeg).append(punc);
                }
            }

            // 如果最后没有以句号或问号结束，则输出剩余内容
            if (accumulatedText.length() > 0) {
                saveToFile(outputFilePath, formatTime(0) + " " + accumulatedText.toString() + "\n");
            }

            logger.info("Processing and saving stamp sents completed. Output file: {}", outputFilePath);
        } catch (IOException e) {
            logger.error("Error processing and saving stamp sents: {}", e.getMessage());
        }
    }

    /**
     * 生成带有时间戳的输出文件路径。
     *
     * @param wav_name
     * @return 输出文件路径
     * @throws IOException 如果创建目录时发生错误
     */
    private static String generateOutputFilePath(String wav_name) throws IOException {
        // 获取项目的根路径
        String projectRootPath = System.getProperty("user.dir");
        // 创建 out 文件夹路径
        String outFolderPath = projectRootPath + "/run/outTxt";
        // 创建 out 文件夹（如果不存在）
        Files.createDirectories(Paths.get(outFolderPath));
        // 生成时间戳文件名
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_");
        String timestamp = sdf.format(new Date());
        // 返回带有时间戳的文件路径
        return outFolderPath + "/" + timestamp + wav_name + ".txt";
    }

    /**
     * 读取JSON文件的内容。
     *
     * @param filePath JSON文件的路径
     * @return 文件内容字符串
     * @throws IOException 如果读取文件时发生错误
     */
    private static String readJsonFile(String filePath) throws IOException {
        // 创建一个 StringBuilder 来存储文件内容
        StringBuilder contentBuilder = new StringBuilder();
        // 使用 BufferedReader 逐行读取文件内容
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        }
        // 返回拼接后的文件内容
        return contentBuilder.toString();
    }

    /**
     * 根据规则调整text_seg字段的内容。
     *
     * @param textSeg 需要调整的text_seg字段内容
     * @return 调整后的字符串
     */
    private static String adjustTextSeg(String textSeg) {
        // 创建一个 StringBuilder 来存储调整后的文本
        StringBuilder adjustedText = new StringBuilder();
        // 标记是否在中文段落中
        boolean inChineseSegment = false;
        // 标记是否刚刚从中文切换到英文
        boolean justSwitchedToEnglish = false;

        // 遍历 textSeg 中的每个字符
        for (int i = 0; i < textSeg.length(); i++) {
            char currentChar = textSeg.charAt(i);

            // 如果当前字符是空格
            if (Character.isWhitespace(currentChar)) {
                // 如果刚刚切换到英文部分，保留这个空格
                if (justSwitchedToEnglish) {
                    adjustedText.append(currentChar);
                    justSwitchedToEnglish = false;
                } else if (!inChineseSegment) {
                    // 当前在英文部分，保留空格
                    adjustedText.append(currentChar);
                }
                continue;
            }

            // 如果当前字符是中文字符
            if (isChinese(currentChar)) {
                inChineseSegment = true;
                adjustedText.append(currentChar);
            } else if (Character.isLetter(currentChar)) {
                // 当前字符是字母
                if (inChineseSegment) {
                    // 刚刚从中文切换到英文，保留这个空格
                    adjustedText.append(' ');
                    justSwitchedToEnglish = true;
                }
                inChineseSegment = false;
                adjustedText.append(currentChar);
            } else {
                // 其他字符（标点符号等）
                adjustedText.append(currentChar);
                inChineseSegment = false;
            }
        }

        // 返回调整后的文本
        return adjustedText.toString();
    }

    /**
     * 判断一个字符是否为中文字符。
     *
     * @param c 需要判断的字符
     * @return 是否为中文字符
     */
    private static boolean isChinese(char c) {
        // 获取字符的 Unicode 块
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        // 检查是否属于中文字符块
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
                ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ||
                ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS ||
                ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }

    /**
     * 将毫秒转换为小时、分钟和秒的格式。
     *
     * @param milliseconds 毫秒数
     * @return 格式化后的时间字符串
     */
    private static String formatTime(long milliseconds) {
        // 计算秒、分、小时
        long secs = milliseconds % 60;
        long mins = (milliseconds / 60) % 60;
        long hours = (milliseconds / 3600);

        // 返回格式化后的时间字符串
        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    /**
     * 将字符串写入到指定的文件中。
     *
     * @param filePath 文件路径
     * @param content  要写入的内容
     * @throws IOException 如果写入文件时发生错误
     */
    private static void saveToFile(String filePath, String content) throws IOException {
        // 使用 BufferedWriter 追加写入内容到文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(content);
        }
    }
}