package com.kacho.my_ocpp.dto.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kacho.my_ocpp.dto.OcppResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusNotificationResponse implements OcppResponse {
    private String status;
}
