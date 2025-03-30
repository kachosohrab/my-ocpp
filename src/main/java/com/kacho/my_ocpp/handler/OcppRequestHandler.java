package com.kacho.my_ocpp.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kacho.my_ocpp.constant.SessionConstants;
import com.kacho.my_ocpp.dto.OcppResponse;
import com.kacho.my_ocpp.dto.impl.*;
import com.kacho.my_ocpp.enums.OcppAction;
import com.kacho.my_ocpp.exception.CallErrorException;
import com.kacho.my_ocpp.service.OcppActionService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;


@Component
public class OcppRequestHandler {

    private final OcppActionService ocppActionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OcppRequestHandler(OcppActionService ocppActionService) {
        this.ocppActionService = ocppActionService;
    }

    public void handle(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Object[] arr = objectMapper.readValue(message.getPayload(), Object[].class);
            if(arr.length<4) throw new CallErrorException("Invalid Message Format");
            if(!(arr[0] instanceof Integer messageType)) throw new CallErrorException("Message Type Should Be An Integer");
            if(messageType<2 || messageType>6) throw new CallErrorException("Requested Message Type Not Supported");
            if(messageType==2) handleCallMessage(session, arr);
            /**
             * Ignore other message types for now [3, 4, 5, 6,]
             */
        } catch (Exception e) {
            session.sendMessage(new TextMessage(e.getMessage()));
        }
    }

    public void afterConnectionClosed(String sessionUuid) {
        ocppActionService.afterConnectionClosed(sessionUuid);
    }

    private void handleCallMessage(WebSocketSession session, Object[] arr) throws IOException {
        if(!(arr[1] instanceof String messageId)) {
            throw new CallErrorException("Message Id Should Be A Unique String");
        }
        if(!(arr[2] instanceof String actionType)) {
            throw new CallErrorException("Action Type Should Be A String");
        }
        OcppAction action = OcppAction.getActionMap().get(actionType);
        if(action==null) {
            throw new CallErrorException("Action Type Should Be One Of The Following: " + OcppAction.getActionNames());
        }
        OcppResponse response;
        switch (action) {
            case BOOT_NOTIFICATION:
                response = ocppActionService.processAction((String) session.getAttributes().get(SessionConstants.SESSION_UUID_KEY),
                        objectMapper.convertValue(arr[3], BootNotificationRequest.class));
                break;
            case HEARTBEAT:
                response = ocppActionService.processAction((String) session.getAttributes().get(SessionConstants.SESSION_UUID_KEY),
                        objectMapper.convertValue(arr[3], HeartBeatRequest.class));
                break;
            case START_TRANSACTION:
                response = ocppActionService.processAction((String) session.getAttributes().get(SessionConstants.SESSION_UUID_KEY),
                        objectMapper.convertValue(arr[3], StartTransactionRequest.class));
                break;
            case STOP_TRANSACTION:
                response = ocppActionService.processAction((String) session.getAttributes().get(SessionConstants.SESSION_UUID_KEY),
                        objectMapper.convertValue(arr[3], StopTransactionRequest.class));
                break;
            case STATUS_NOTIFICATION:
                response = ocppActionService.processAction((String) session.getAttributes().get(SessionConstants.SESSION_UUID_KEY),
                        objectMapper.convertValue(arr[3], StatusNotificationRequest.class));
                break;
            default:
                session.sendMessage(new TextMessage("Action Type Not Supported For CALL"));
                return;
        }
        Object[] responseArr = new Object[3];
        responseArr[0] = 3;
        responseArr[1] = arr[1];
        responseArr[2] = response;
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(responseArr)));
    }
}
