package com.zjs.web_mib_browser.telnet;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.net.telnet.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Telnet连接，处理Telnet发送、响应接收(处理回显，并不算优雅)
 *
 * @author pengxg
 * @date 2025-04-07 13:33
 */
public class TelnetConnection {
    private final WebSocketSession session;
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    private TelnetClient telnet;
    private InputStream in;
    private OutputStream out;
    private Thread readThread;

    // 最后输入的命令——处理回显
    private String lastCommand = "";

    public TelnetConnection(WebSocketSession session, String host, int port,
                            String username, String password) {
        this.session = session;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void connect() throws IOException, InterruptedException, InvalidTelnetOptionException {
        telnet = new TelnetClient();
        telnet.setConnectTimeout(10000); // 10秒连接超时
        // 禁用回显
        TelnetOptionHandler echoOption = new EchoOptionHandler();
        telnet.addOptionHandler(echoOption);
        telnet.connect(host, port);

        in = telnet.getInputStream();
        out = telnet.getOutputStream();

        // 处理登录流程: 如果无username或密码，应当让用户在webssh界面输入
        if (StrUtil.isAllNotBlank(username, password)) {
            waitForAndRespond("login:", username);
            waitForAndRespond("password:", password);
        }

        // 启动读取线程
        readThread = new Thread(this::readFromTelnet);
        readThread.start();
    }

    private void waitForAndRespond(String prompt, String response)
            throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        long timeout = System.currentTimeMillis() + 1000 * 60 * 10; // 10分钟超时

        while (System.currentTimeMillis() < timeout) {
            if (in.available() > 0) {
                int len = in.read(buffer);
                if (len > 0) {
                    String data = new String(buffer, 0, len);
                    sb.append(data);
                    sendToWebSocket(data);

                    if (sb.toString().toLowerCase().contains(prompt.toLowerCase())) {
                        out.write((response + "\r\n").getBytes());
                        out.flush();
                        return;
                    }
                }
            }
            Thread.sleep(100);
        }
        throw new IOException("等待提示超时: " + prompt);
    }

    private void readFromTelnet() {
        byte[] buffer = new byte[1024];
        try {
            while (!Thread.currentThread().isInterrupted() && telnet.isConnected()) {
                if (in.available() > 0) {
                    int len = in.read(buffer);
                    if (len > 0) {
                        String data = new String(buffer, 0, len);
                        // 处理回显，避免重复
                        if (data.startsWith(lastCommand)) {
                            data = data.substring(lastCommand.length());
                        }
                        sendToWebSocket(data);
                    }
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {
            if (!Thread.currentThread().isInterrupted()) {
                sendToWebSocket("\r\nTelnet连接中断: " + e.getMessage() + "\r\n");
            }
        }
    }

    public void sendCommand(String command) throws IOException {
        if (out != null && telnet.isConnected()) {
            // 如果command以\t结尾则取\t之前的内容作为lastCommand
//            if (command.endsWith("\t")) {
            lastCommand = command.substring(0, command.length() - 1);
            out.write((command).getBytes());
//            } else {
//                lastCommand = command;
//                out.write((command + "\r\n").getBytes());
//            }
            out.flush();
        }
    }

    public void disconnect() {
        if (readThread != null) {
            readThread.interrupt();
        }
        try {
            if (telnet != null && telnet.isConnected()) {
                telnet.disconnect();
            }
        } catch (IOException e) {
            // 忽略关闭时的异常
        }
    }

    private void sendToWebSocket(String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            // 发送失败时中断读取线程
            Thread.currentThread().interrupt();
        }
    }
}