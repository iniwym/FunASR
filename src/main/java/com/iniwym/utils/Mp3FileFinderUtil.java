package com.iniwym.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 获取mp3文件
 * @Author: iniwym
 * @Date: 2025-01-13
 */
public class Mp3FileFinderUtil {

    private static final Logger logger = LoggerFactory.getLogger(Mp3FileFinderUtil.class);

    /**
     * 主方法，用于调用查找 .mp3 文件的逻辑。
     */
    public static void main(String[] args) {
        // 目录路径
        String dirPath = "/test/mp3";

        // 查找并获取所有 .mp3 文件路径
        String[] mp3FilePaths = findMp3FilesInDirectory(dirPath);

        // 打印结果
        for (String filePath : mp3FilePaths) {
            logger.info("Found MP3 file: " + filePath);
        }
    }

    /**
     * 查找指定目录下的所有 .mp3 文件，并返回其路径组成的数组。
     *
     * @param dirPath 要查找的目录路径
     * @return 包含所有 .mp3 文件路径的字符串数组
     */
    public static String[] findMp3FilesInDirectory(String dirPath) {
        File directory = new File(dirPath);

        if (!directory.exists() || !directory.isDirectory()) {

            logger.error("The provided path is not a directory or does not exist.");
            return new String[0];  // 返回空数组
        }

        List<String> mp3FilePaths = new ArrayList<>();
        findMp3FilesRecursive(directory, mp3FilePaths);
        return mp3FilePaths.toArray(new String[0]);
    }

    /**
     * 递归查找指定目录下的所有 .mp3 文件，并将其路径添加到列表中。
     *
     * @param dir          要查找的目录
     * @param mp3FilePaths 存储 .mp3 文件路径的列表
     */
    private static void findMp3FilesRecursive(File dir, List<String> mp3FilePaths) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 如果是子目录，则递归查找
                    findMp3FilesRecursive(file, mp3FilePaths);
                } else if (file.isFile() && file.getName().toLowerCase().endsWith(".mp3")) {
                    // 如果是 .mp3 文件，则添加其路径到列表
                    mp3FilePaths.add(file.getAbsolutePath());
                }
            }
        }
    }
}
