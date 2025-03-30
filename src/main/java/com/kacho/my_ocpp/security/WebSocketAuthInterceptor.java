package com.kacho.my_ocpp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtility jwtUtility;



    public WebSocketAuthInterceptor(JwtUtility jwtUtility) {
        this.jwtUtility = jwtUtility;
    }


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if(request instanceof ServletServerHttpRequest servletServerHttpRequest) {
            String token = servletServerHttpRequest.getServletRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if(token!=null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                if( jwtUtility.isTokenValid(token)) {
                    String subject = jwtUtility.extractSubject(token);
                    attributes.put("sub", subject);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
