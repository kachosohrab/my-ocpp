package com.kacho.my_ocpp.dto.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kacho.my_ocpp.dto.OcppRequest;
import com.kacho.my_ocpp.enums.OcppAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Ignores extra fields in JSON
public class StatusNotificationRequest implements OcppRequest {

    private int connectorId;
    private String status;
    private String errorCode;
    private String timestamp;

    @Override
    public OcppAction getOcppAction() {
        return OcppAction.STATUS_NOTIFICATION;
    }

}
