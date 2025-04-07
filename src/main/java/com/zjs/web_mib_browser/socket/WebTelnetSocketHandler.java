package com.zjs.web_mib_browser.socket;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.zjs.web_mib_browser.domain.Connection;
import com.zjs.web_mib_browser.service.ConnectionService;
import com.zjs.web_mib_browser.telnet.TelnetConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pengxg
 * @date 2025-04-02 15:33
 */
@Slf4j
@Component
public class WebTelnetSocketHandler extends TextWebSocketHandler {
    @Resource
    private ConnectionService connectionService;

    private final ConcurrentHashMap<String, TelnetConnection> connections = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSSH Socket连接建立: ", session.getUri().toString());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JSONObject jsonObject = JSONObject.parseObject(payload);
        String type = jsonObject.getString("type");

        if ("connect".equals(type)) {
            String ip = jsonObject.getString("ip");
            Connection connection = connectionService.getByIp(ip);
            if (connection == null) {
                log.error("不存在ip={}的连接, 无法建立ssh连接", ip);
                session.sendMessage(new TextMessage("不存在ip=" + ip + "的连接"));
                return;
            }

            int port = ObjectUtil.defaultIfNull(connection.getSshPort(), 23);
            String username = connection.getSshUsername();
            String password = connection.getSshPassword();

//            disconnect(session);

            TelnetConnection conn = new TelnetConnection(session, ip, port, username, password);
            connections.put(session.getId(), conn);
            try {
                conn.connect();
                session.sendMessage(new TextMessage("\r\nTelnet连接成功!\r\n"));
            } catch (Exception e) {
                log.error("Telnet连接失败,ip:" + ip, e);
                session.sendMessage(new TextMessage("\r\nTelnet连接失败,ip=" + ip + "\r\n"));
                disconnect(session);
            }
        } else if ("command".equals(type)) {
            // 处理命令输入
            TelnetConnection conn = connections.get(session.getId());
            if (conn != null) {
                String command = jsonObject.getString("command");
                try {
                    conn.sendCommand(command);
                } catch (IOException e) {
                    log.error("Telnet发送命令失败", e);
                    session.sendMessage(new TextMessage("\r\nTelnet发送命令失败" + "\r\n"));
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSSH Socket传输错误, ip:" + session.getUri(), exception);
        disconnect(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSSH Socket连接关闭: {}", session.getUri());
        disconnect(session);
    }

    private void disconnect(WebSocketSession session) {
        TelnetConnection connection = connections.remove(session.getId());
        if (connection != null) {
            connection.disconnect();
        }
        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (IOException e) {
            log.error("WebSSH Socket关闭异常", e);
        }
    }
}
