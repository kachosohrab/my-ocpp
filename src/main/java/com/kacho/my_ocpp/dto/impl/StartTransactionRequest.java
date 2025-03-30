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
public class StartTransactionRequest implements OcppRequest {
    private int connectorId;
    private String idTag;
    private String timestamp;
    private int meterStart;

    @Override
    public OcppAction getOcppAction() {
        return OcppAction.START_TRANSACTION;
    }
}
