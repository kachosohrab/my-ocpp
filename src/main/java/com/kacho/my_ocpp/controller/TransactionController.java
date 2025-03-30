package com.kacho.my_ocpp.controller;

import com.kacho.my_ocpp.dto.impl.ChargePointTransactionHistory;
import com.kacho.my_ocpp.service.OcppActionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final OcppActionService ocppActionService;

    public TransactionController(OcppActionService ocppActionService) {
        this.ocppActionService = ocppActionService;
    }

    @GetMapping("/charge-point")
    public ResponseEntity<ChargePointTransactionHistory> getChargePointTransactionHistory(
            @RequestParam String serialNumber,
            @RequestParam String startTime,
            @RequestParam String endTime
    ) {
        return ResponseEntity.ok(ocppActionService.getChargePointTransactionHistory(serialNumber, startTime, endTime));
    }

}
