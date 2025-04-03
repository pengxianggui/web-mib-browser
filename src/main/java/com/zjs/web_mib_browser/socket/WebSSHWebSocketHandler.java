package com.zjs.web_mib_browser.socket;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.zjs.web_mib_browser.domain.Connection;
import com.zjs.web_mib_browser.service.ConnectionService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * @author pengxg
 * @date 2025-04-02 15:33
 */
@Slf4j
@Component
public class WebSSHWebSocketHandler implements WebSocketHandler {
    @Resource
    private ConnectionService connectionService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSSH Socket连接建立: ", session.getUri().toString());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (!(message instanceof TextMessage)) {
            return;
        }
        String payload = ((TextMessage) message).getPayload();
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
            if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
                log.error("请先进入【连接管理】维护连接(ip={})的ssh用户名和密码", ip);
                session.sendMessage(new TextMessage("请先维护连接(ip=" + ip + ")的ssh用户名和密码"));
                return;
            }

            // 创建SSH连接
            SSHConnectInfo sshConnectInfo = new SSHConnectInfo();
            JSch jsch = new JSch();
            Session sshSession = jsch.getSession(username, ip, port);
            sshSession.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
            config.put("server_host_key", "ssh-rsa,ssh-dss,ecdsa-sha2-nistp256");
            sshSession.setConfig(config);
            sshSession.connect();

            Channel channel = sshSession.openChannel("shell");
            ((ChannelShell) channel).setPtyType("xterm");
            InputStream inputFromChannel = channel.getInputStream();
            OutputStream outputToChannel = channel.getOutputStream();
            channel.connect();

            sshConnectInfo.setSshSession(sshSession);
            sshConnectInfo.setChannel(channel);
            sshConnectInfo.setInputFromChannel(inputFromChannel);
            sshConnectInfo.setOutputToChannel(outputToChannel);

            session.getAttributes().put("ssh", sshConnectInfo);

            // 启动线程读取SSH输出
            new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    while (channel.isConnected()) {
                        int i = inputFromChannel.read(buffer);
                        if (i > 0) {
                            session.sendMessage(new TextMessage(new String(buffer, 0, i)));
                        }
                    }
                } catch (Exception e) {
                    log.error("SSH读取异常", e);
                }
            }).start();
        } else if ("command".equals(type)) {
            // 处理命令输入
            SSHConnectInfo sshConnectInfo = (SSHConnectInfo) session.getAttributes().get("ssh");
            if (sshConnectInfo != null && sshConnectInfo.getOutputToChannel() != null) {
                String command = jsonObject.getString("command");
                sshConnectInfo.getOutputToChannel().write(command.getBytes());
                sshConnectInfo.getOutputToChannel().flush();
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSSH Socket传输错误, ip:" + session.getUri(), exception);
        closeSSHConnection(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSSH Socket连接关闭: {}", session.getUri());
        closeSSHConnection(session);
    }

    private void closeSSHConnection(WebSocketSession session) throws IOException {
        SSHConnectInfo sshConnectInfo = (SSHConnectInfo) session.getAttributes().get("ssh");
        if (sshConnectInfo != null) {
            if (sshConnectInfo.getChannel() != null) {
                sshConnectInfo.getChannel().disconnect();
            }
            if (sshConnectInfo.getSshSession() != null) {
                sshConnectInfo.getSshSession().disconnect();
            }
        }
        session.close();
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Data
    class SSHConnectInfo {
        private Session sshSession;
        private Channel channel;
        private InputStream inputFromChannel;
        private OutputStream outputToChannel;
    }
}
