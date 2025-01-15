package com.iniwym;

import com.iniwym.client.FunasrWsClient;
import com.iniwym.utils.ConfigReaderUtil;
import com.iniwym.utils.Mp3FileFinderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @Description: 主方法
 * @Author: iniwym
 * @Date: 2025-01-14
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        // 服务器IP地址
        String srvIp = ConfigReaderUtil.getPropertyKey("srvIp");
        // 服务器端口号
        String srvPort = ConfigReaderUtil.getPropertyKey("srvPort");
        // 音频文件目录
        String mp3Path = ConfigReaderUtil.getPropertyKey("mp3Path");

        if (args.length > 0) {
            // 音频文件目录，从命令行参数获取
            mp3Path = args[0];
        }
        logger.info("音频文件目录：{}", mp3Path);
        System.out.println("音频路径: " + mp3Path + "\n处理音频文件中...");

        // 处理音频文件
        handleAudio(srvIp, srvPort, mp3Path);
    }

    /**
     * 处理音频文件
     *
     * @param srvIp   服务器IP地址
     * @param srvPort 服务器端口号
     * @param mp3Path 音频文件目录
     */
    private static void handleAudio(String srvIp, String srvPort, String mp3Path) {
        try {
            // 获取mp3文件列表
            String[] mp3Files = Mp3FileFinderUtil.findMp3FilesInDirectory(mp3Path);

            // 创建并启动一个线程来处理客户端任务
            Thread clientTask = new Thread(() -> {
                try {
                    // 构造WebSocket地址
                    String wsAddress = "ws://" + srvIp + ":" + srvPort;
                    // 打印WebSocket地址
                    logger.info("WebSocket地址：{}", wsAddress);

                    // 创建FunasrWsClient实例
                    FunasrWsClient funasrWsClient = new FunasrWsClient(new URI(wsAddress));
                    // 设置文件路径
                    funasrWsClient.setWavPath(mp3Files);
                    // 建立WebSocket连接
                    funasrWsClient.connect();
                } catch (Exception e) {
                    // 打印异常信息
                    logger.error("建立WebSocket连接失败: ", e);
                }
            });
            clientTask.start();

        } catch (Exception e) {
            // 打印处理音频失败的异常信息
            logger.error("处理音频失败：", e);
        }
    }

}
