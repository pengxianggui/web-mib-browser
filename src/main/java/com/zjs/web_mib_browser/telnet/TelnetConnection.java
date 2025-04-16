package com.zjs.web_mib_browser.telnet;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

    public void sendCommand(String command) throws IOException {
        if (out != null && telnet.isConnected()) {
            // 将command末尾可能的\r换成\r\n
            if (command.endsWith("\r")) {
                command = command.substring(0, command.length() - 1);
                command = command + "\r\n";
            }
            out.write((command).getBytes());
            out.flush();
        }
    }

    private void readFromTelnet() {
        byte[] buffer = new byte[1024];
        try {
            while (!Thread.currentThread().isInterrupted() && telnet.isConnected()) {
                if (in.available() > 0) {
                    int len = in.read(buffer);
                    if (len > 0) {
                        String data = new String(buffer, 0, len);
                        sendToWebSocket(data);
                    }
                }
            }
        } catch (Exception e) {
            if (!Thread.currentThread().isInterrupted()) {
                sendToWebSocket("\r\nTelnet连接中断: " + e.getMessage() + "\r\n");
            }
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
            log.error("disconnect error", e);
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