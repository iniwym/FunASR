package com.iniwym.client;

import java.io.*;
import java.net.URI;

import com.iniwym.utils.ConfigReaderUtil;
import com.iniwym.utils.JsonToFileUtil;
import com.iniwym.utils.JsonToTxtUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.iniwym.utils.LarkUtils.larkBotMessage;

/**
 * @Description: FunasrWsClient类继承自WebSocketClient，用于实现与服务器的WebSocket连接，
 * 并进行语音识别的客户端功能。该类包括连接到服务器、发送语音数据、
 * 接收识别结果等功能。
 * 语音识别
 * @Author: iniwym
 * @Date: 2025-01-10
 */
public class FunasrWsClient extends WebSocketClient {
    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(FunasrWsClient.class);

    private boolean iseof = false;
    private String[] wavPaths; // 修改为字符串数组
    private int currentFileIndex = 0; // 当前处理的文件索引
    private final String mode = ConfigReaderUtil.getPropertyKey("mode");
    private final String strChunkSize = ConfigReaderUtil.getPropertyKey("strChunkSize");
    private final String hotwords = ConfigReaderUtil.getPropertyKey("hotwords");
    private final String fsthotwords = ConfigReaderUtil.getPropertyKey("fsthotwords");
    private final int chunkInterval = Integer.parseInt(ConfigReaderUtil.getPropertyKey("chunkInterval"));
    private final int sendChunkSize = Integer.parseInt(ConfigReaderUtil.getPropertyKey("sendChunkSize"));


    /**
     * 计算发送分块大小
     * 该方法用于计算向FunasrWsClient发送语音数据时的分块大小
     * 分块大小基于配置的字符串和采样率进行计算，旨在优化数据传输效率
     *
     * @return 返回计算得到的发送分块大小
     */
    public int countSendChunkSize() {
        // 采样率，固定值为16000Hz
        int RATE = 16000;

        // 分割分块大小配置字符串，以逗号分隔
        String[] chunkList = strChunkSize.split(",");

        // 计算分块大小，单位为毫秒
        int int_chunk_size = 60 * Integer.parseInt(chunkList[1].trim()) / chunkInterval;

        // 计算CHUNK值，用于后续的语音数据分块传输
        int CHUNK = RATE / 1000 * int_chunk_size;

        // 计算步长值，用于确定每次传输的字节数
        int stride = 60 * Integer.parseInt(chunkList[1].trim()) / chunkInterval / 1000 * 16000 * 2;

        // 打印分块大小、CHUNK和步长的计算结果
        logger.info("chunk_size:" + int_chunk_size);
        logger.info("CHUNK:" + CHUNK);
        logger.info("stride:" + stride);

        // 配置FunasrWsClient的发送分块大小
        int sendChunkSize = CHUNK * 2;

        // 打印发送分块大小
        logger.info("sendChunkSize:" + sendChunkSize);
        // 返回计算得到的发送分块大小
        return sendChunkSize;
    }

    public void setWavPath(String[] wavPaths) { // 修改为接受字符串数组
        this.wavPaths = wavPaths;
    }

    /**
     * FunasrWsClient构造函数，初始化WebSocket连接的服务器URI。
     *
     * @param serverURI 服务器URI
     */
    public FunasrWsClient(URI serverURI) {
        super(serverURI);
    }

    /**
     * 发送JSON格式的请求参数到服务器。
     *
     * @param isSpeaking 是否正在说话
     */
    public void sendJson(boolean isSpeaking) {

        try {
            String wavPath = wavPaths[currentFileIndex];

            // 提取文件后缀名
            String suffix = wavPath.split("\\.")[wavPath.split("\\.").length - 1];
            // 提取文件名
            String wavName = wavPath.substring(wavPath.lastIndexOf(File.separator) + 1).split("\\.")[0];

            // 创建JSON对象并填充参数
            JSONObject obj = new JSONObject();
            obj.put("mode", mode);
            JSONArray array = new JSONArray();
            // 分割并转换分块大小配置字符串为整数数组
            String[] chunkList = strChunkSize.split(",");
            for (int i = 0; i < chunkList.length; i++) {
                array.add(Integer.valueOf(chunkList[i].trim()));
            }

            obj.put("chunk_size", array);
            obj.put("chunk_interval", new Integer(chunkInterval));
            obj.put("wav_name", wavName);

            // 处理热词配置，如果存在则添加到JSON对象中
            if (hotwords.trim().length() > 0) {
                String regex = "\\d+";
                JSONObject jsonitems = new JSONObject();
                String[] items = hotwords.trim().split(" ");
                Pattern pattern = Pattern.compile(regex);
                String tmpWords = "";
                for (int i = 0; i < items.length; i++) {

                    Matcher matcher = pattern.matcher(items[i]);

                    if (matcher.matches()) {

                        jsonitems.put(tmpWords.trim(), items[i].trim());
                        tmpWords = "";
                        continue;
                    }
                    tmpWords = tmpWords + items[i] + " ";

                }

                obj.put("hotwords", jsonitems.toString());
            }

            // 根据音频文件后缀名设置格式
            if (suffix.equals("wav")) {
                suffix = "pcm";
            }
            obj.put("wav_format", suffix);

            // 添加是否正在说话的状态
            if (isSpeaking) {
                obj.put("is_speaking", new Boolean(true));
            } else {
                obj.put("is_speaking", new Boolean(false));
            }

            // 记录发送的JSON对象日志
            logger.info("sendJson: " + obj);
            // 发送JSON字符串到服务器
            send(obj.toString());

            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在语音识别结束时发送EOF信号到服务器。
     * 此方法用于通知服务器语音输入已经结束，以便服务器可以开始处理语音数据。
     * 它通过发送一个包含{"is_speaking": false}的JSON对象来实现。
     */
    public void sendEof() {
        try {
            // 创建一个JSON对象来封装语音结束的信号。
            JSONObject obj = new JSONObject();

            // 设置JSON对象中的"is_speaking"字段为false，表示语音输入结束。
            obj.put("is_speaking", new Boolean(false));

            // 记录发送的EOF信号内容。
            logger.info("sendEof: " + obj);

            // 发送EOF信号到服务器。
            send(obj.toString());
            // 设置iseof标志为true，表示EOF信号已经发送。
            iseof = true;
            return;
        } catch (Exception e) {
            // 记录发送EOF信号时发生的错误。
            logger.error("sendEof error: " + e.getMessage());
        }
    }

    /**
     * 读取并发送音频数据进行识别。
     */
    public void recWav() {

        if (currentFileIndex >= wavPaths.length) {
            logger.info("All files processed.");
            return;
        }

        String wavPath = wavPaths[currentFileIndex];
        // 当前处理的文件是
        logger.info("当前处理的文件是: " + wavPath);
        System.out.println("当前处理的文件是: " + wavPath);
        // 发送JSON格式的请求参数
        sendJson(true);

        // 创建File对象以读取音频文件
        File file = new File(wavPath);

        // 定义每个数据块的大小
        int chunkSize = sendChunkSize;
        // 创建一个字节数组用于存储读取的数据块
        byte[] bytes = new byte[chunkSize];

        // 初始化已读取的字节数
        int readSize = 0;
        try (FileInputStream fis = new FileInputStream(file)) {
            // 如果是wav文件，则跳过前44字节的wav头
            if (wavPath.endsWith(".wav")) {
                fis.read(bytes, 0, 44); //skip first 44 wav header
            }
            // 读取第一个数据块
            readSize = fis.read(bytes, 0, chunkSize);
            // 循环读取数据块直到文件结束
            while (readSize > 0) {
                // 当读取的数据块大小等于预设的数据块大小时发送数据
                if (readSize == chunkSize) {
                    send(bytes); // send buf to server

                } else {
                    // 当读取的数据块大小不等于预设的数据块大小时（通常是文件的最后一个数据块），创建一个新的字节数组并发送数据
                    byte[] tmpBytes = new byte[readSize];
                    for (int i = 0; i < readSize; i++) {
                        tmpBytes[i] = bytes[i];
                    }
                    send(tmpBytes);
                }
                // 如果不是离线模式，通过睡眠模拟在线流式传输的延迟
                if (!mode.equals("offline")) {
                    Thread.sleep(Integer.valueOf(chunkSize / 32));
                }

                // 读取下一个数据块
                readSize = fis.read(bytes, 0, chunkSize);
            }

            // 根据模式决定是否发送结束符以及是否等待一段时间后关闭连接
            if (!mode.equals("offline")) {
                // 如果不是离线模式，发送结束符后等待3秒关闭连接
                Thread.sleep(2000);
                sendEof();
                Thread.sleep(3000);
                close();
            } else {
                // 如果是离线模式，直接发送结束符
                sendEof();
            }

        } catch (Exception e) {
            // 打印异常信息
            logger.error("处理音频文件：{} 失败", wavPath, e);
        }

    }

    /**
     * 当WebSocket连接打开时调用。
     *
     * @param serverHandshake 服务器握手数据
     */
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        // 创建一个可运行对象，指向recWav方法
        Runnable recWavThread = this::recWav;

        // 新建一个线程来执行recWav方法
        Thread thread = new Thread(recWavThread);

        // 启动线程开始执行recWav方法
        thread.start();
    }

    /**
     * 当接收到服务器消息时调用。
     *
     * @param message 服务器消息
     */
    @Override
    public void onMessage(String message) {

        // 初始化一个空的JSON对象，用于解析接收到的消息
        JSONObject jsonObject = new JSONObject();
        // 记录接收到的消息
        logger.debug("received: " + message);

        try {
            // 将接收到的消息解析为JSON对象
            jsonObject = JSON.parseObject(message);
            // 将解析后的JSON对象写入文件
            jsonToFile(jsonObject);

            currentFileIndex++; // 处理完一个文件后，索引加一
            if (currentFileIndex < wavPaths.length) {
                // 处理下一个文件
                recWav();
            } else {
                String result = "所有音频处理完毕...";
                logger.info(result);
                System.out.println(result);
                larkBotMessage("Funasr 语音识别", result);
                close();
            }

        } catch (Exception e) {
            // 记录JSON消息处理过程中的错误
            logger.error("Error processing JSON message: " + e.getMessage());
        }
        // 当处于离线模式且接收到的消息不是最终消息时，关闭连接
        if (iseof && mode.equals("offline") && !jsonObject.containsKey("is_final")) {
            close();
        }

        // 当处于离线模式且接收到的消息是最终消息，但不是预期的最终消息时，关闭连接
        if (iseof && mode.equals("offline") && jsonObject.containsKey("is_final") && jsonObject.get("is_final").equals("false")) {
            close();
        }
    }


    /**
     * 将JSONObject转换为JSON文件并进一步处理
     * 此方法首先将给定的JSONObject保存到一个JSON文件中，
     * 然后调用另一个方法来处理这个JSON对象并将其内容保存到一个txt文件中
     *
     * @param jsonObject 要保存和处理的JSON对象
     */
    private void jsonToFile(JSONObject jsonObject) {

        // 处理响应并保存JSON文件中
        String jsonFilePath = JsonToFileUtil.saveJsonObjectToFile(jsonObject);

        // 处理json对象并保存到txt文件中
        JsonToTxtUtil.processAndSaveStampSents(jsonFilePath);
    }

    /**
     * 当WebSocket连接关闭时调用的方法
     *
     * @param code   关闭连接的状态码，表示关闭的原因
     * @param reason 关闭连接的描述信息，提供更多细节说明
     * @param remote 表示连接是否由远程 peer 关闭
     *               <p>
     *               此方法主要用于处理WebSocket连接关闭事件它记录了关闭连接的相关信息，
     *               包括关闭方（本地或远程）、关闭状态码和关闭原因，以便于调试和审计
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        // 记录连接关闭信息，动态确定关闭方
        logger.info("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
    }

    /**
     * 当发生错误时调用的方法
     *
     * @param ex 异常对象，提供了错误的详细信息
     *           <p>
     *           此方法主要用于处理错误情况，通过日志记录错误信息
     *           这样做可以帮助开发者诊断问题，确保错误不被忽视
     */
    @Override
    public void onError(Exception ex) {
        logger.error("onError: " + ex);
    }

}

