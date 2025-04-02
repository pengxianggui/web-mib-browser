package com.zjs.web_mib_browser.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pengxg
 * @date 2025-04-02 9:59
 */
@Slf4j
public class MsgWebSocketHandler extends TextWebSocketHandler {
    private static final List<WebSocketSession> sessions = new ArrayList<>();
    private ObjectMapper objectMapper;

    public MsgWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.debug("Connected to WebSocket, url: {}", session.getUri().toString());
        session.sendMessage(new TextMessage("Connected to WebSocket!"));
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String msg = message.getPayload();
        if ("ping".equals(msg)) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.debug("Connection closed, url: {}", session.getUri().toString());
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Connection transport error, url: {}, msg: {}", session.getUri().toString(), exception.getMessage());
    }

    public void boardcast(Type type, Object payload) {
        try {
            String data = objectMapper.writeValueAsString(new Message(type, payload));
            for (WebSocketSession session : sessions) {
                session.sendMessage(new TextMessage(data));
            }
        } catch (JsonProcessingException e) {
            log.error("payload序列化错误", e);
        } catch (IOException e) {
            log.error("服务端发送websocket异常", e);
        }
    }

    public enum Type {
        CONNECTION_REACHABLE_REFRESH
    }

    @Data
    public static class Message {
        private Type type;
        private Object payload;

        public Message(Type type, Object payload) {
            this.type = type;
            this.payload = payload;
        }
    }
}
