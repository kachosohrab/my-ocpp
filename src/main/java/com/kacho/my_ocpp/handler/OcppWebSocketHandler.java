package com.kacho.my_ocpp.handler;

import com.kacho.my_ocpp.constant.SessionConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OcppWebSocketHandler extends TextWebSocketHandler {

    private final OcppRequestHandler ocppRequestHandler;
    private static final Map<String, WebSocketSession> uuidToSessionMap = new ConcurrentHashMap<>();

    public  OcppWebSocketHandler(OcppRequestHandler ocppRequestHandler) {
        this.ocppRequestHandler = ocppRequestHandler;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID uuid = UUID.randomUUID();
        session.getAttributes().put(SessionConstants.SESSION_UUID_KEY, uuid.toString());
        uuidToSessionMap.put(uuid.toString(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ocppRequestHandler.handle(session, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionUuid = (String) session.getAttributes().get(SessionConstants.SESSION_UUID_KEY);

        if (sessionUuid != null) {
            ocppRequestHandler.afterConnectionClosed(sessionUuid);
            uuidToSessionMap.remove(sessionUuid);
        }
    }

    public void expireConnection(String uuid) {
        uuidToSessionMap.computeIfPresent(uuid, (k, v) -> {
            try {
                v.close(CloseStatus.GOING_AWAY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return v;
        });
        uuidToSessionMap.remove(uuid);
    }
}
