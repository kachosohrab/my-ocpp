package com.kacho.my_ocpp.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class RedisExpiryListener implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(RedisExpiryListener.class);

    private final OcppWebSocketHandler webSocketHandler;

    public RedisExpiryListener(OcppWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredSessionUUID = message.toString();
        logger.info("Expired Key Event Captured By Redis For the Key: " + expiredSessionUUID);
        webSocketHandler.expireConnection(expiredSessionUUID);
    }
}
