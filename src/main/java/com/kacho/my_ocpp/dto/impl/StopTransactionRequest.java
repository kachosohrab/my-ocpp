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
public class StopTransactionRequest implements OcppRequest {

    private String transactionId;
    private String timestamp;
    private int meterStop;
    private String reason;

    @Override
    public OcppAction getOcppAction() {
        return OcppAction.STOP_TRANSACTION;
    }
}
