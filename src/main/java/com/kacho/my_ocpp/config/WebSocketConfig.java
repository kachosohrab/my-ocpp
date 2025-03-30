package com.kacho.my_ocpp.config;

import com.kacho.my_ocpp.handler.OcppWebSocketHandler;
import com.kacho.my_ocpp.security.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OcppWebSocketHandler ocppWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(OcppWebSocketHandler ocppWebSocketHandler,
                           WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.ocppWebSocketHandler = ocppWebSocketHandler;
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(this.ocppWebSocketHandler, "/ocpp")
                .setAllowedOrigins("*")
                .addInterceptors(this.webSocketAuthInterceptor);
    }
}
