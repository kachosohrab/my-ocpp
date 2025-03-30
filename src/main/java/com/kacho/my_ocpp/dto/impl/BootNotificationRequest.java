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
public class BootNotificationRequest implements OcppRequest {

    private String id;
    private String chargePointModel;
    private String chargePointVendor;
    private String firmwareVersion;
    private String chargePointSerialNumber;

    @Override
    public OcppAction getOcppAction() {
        return OcppAction.BOOT_NOTIFICATION;
    }
}
