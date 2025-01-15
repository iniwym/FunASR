package com.iniwym.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 将jsonObject格式化并输出到文件
 * @Author: iniwym
 * @Date: 2025-01-13
 */
public class JsonToFileUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonToFileUtil.class);

    /**
     * 主方法，用于调用处理和保存 JSON 对象的逻辑。
     */
    public static void main(String[] args) {
        // 示例 JSON 字符串（实际应用中可以是从其他程序传递过来的）
        String message = "{\"name\":\"张三\",\"age\":30,\"address\":{\"city\":\"北京\",\"zip\":\"100000\"}}";

        // 将 JSON 字符串解析为 JSONObject
        JSONObject jsonObject = JSON.parseObject(message);

        // 调用处理并保存的方法
        saveJsonObjectToFile(jsonObject);
    }

    /**
     * 生成带有时间戳的输出文件路径。
     *
     * @return 输出文件路径
     */
    private static String generateOutputFilePath() throws IOException {
        // 获取项目的根路径
        String projectRootPath = System.getProperty("user.dir");

        // 创建 out 文件夹路径
        String outFolderPath = projectRootPath + "/run/outJson";

        // 创建 out 文件夹（如果不存在）
        Files.createDirectories(Paths.get(outFolderPath));

        // 生成时间戳文件名
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());

        // 返回带有时间戳的文件路径
        return outFolderPath + "/" + timestamp + ".json";
    }

    /**
     * 将 JSONObject 对象保存为格式化的 JSON 文件。
     *
     * @param jsonObject 需要保存的 JSONObject 对象
     */
    public static String saveJsonObjectToFile(JSONObject jsonObject) {
        try {
            // 生成带有时间戳的输出文件路径
            String outputFilePath = generateOutputFilePath();

            // 将 JSONObject 转换为格式化的 JSON 字符串
            String formattedJson = JSON.toJSONString(jsonObject, true);

            // 使用 BufferedWriter 写入文件
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
                writer.write(formattedJson);
            }
            logger.info("JSON object saved to file: " + outputFilePath);
            return outputFilePath;
        } catch (IOException e) {
            logger.error("Error saving JSON object to file: " + e.getMessage(), e);
        }
        return null;
    }
}
