package com.kacho.my_ocpp.dto.impl;

import com.kacho.my_ocpp.dto.OcppResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HeartBeatResponse implements OcppResponse {
    private String currentTime;
}
