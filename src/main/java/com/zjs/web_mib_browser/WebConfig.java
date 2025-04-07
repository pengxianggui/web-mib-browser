package com.zjs.web_mib_browser;

import com.zjs.web_mib_browser.socket.MsgWebSocketHandler;
import com.zjs.web_mib_browser.socket.WebTelnetSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author pengxg
 * @date 2025/3/30 10:40
 */
@EnableWebSocket
@Configuration
public class WebConfig implements WebMvcConfigurer, WebSocketConfigurer {

    @Value("${web-socket.path:/socket}")
    private String socketPath;
    @Value(("${web-socket.ssh-path://webssh}"))
    private String webSSHPath;

    @Resource
    private MsgWebSocketHandler msgWebSocketHandler;
    @Resource
    private WebTelnetSocketHandler webTelnetSocketHandler;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 移除默认的 StringHttpMessageConverter
        converters.removeIf(converter -> converter instanceof StringHttpMessageConverter);

        // 添加自定义的 HttpMessageConverter（如果需要）
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        converters.add(jsonConverter);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(msgWebSocketHandler, socketPath)
                .addHandler(webTelnetSocketHandler, webSSHPath)
                .setAllowedOrigins("*");
    }
}
