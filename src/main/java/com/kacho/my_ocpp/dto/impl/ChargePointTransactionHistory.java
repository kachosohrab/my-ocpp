package com.kacho.my_ocpp.dto.impl;

import com.kacho.my_ocpp.model.ChargePoint;
import com.kacho.my_ocpp.model.Connector;
import com.kacho.my_ocpp.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChargePointTransactionHistory {

    private String serialNumber;
    private String lastUpdated;
    private String lastHeartBeat;
    private List<TransactionSummary> transactions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class TransactionSummary {

        private int connectorId;
        private String idTag;
        private String startTime;
        private String endTime;
        private int meterStart;
        private int meterStop;
    }

    private ChargePointTransactionHistory(){}

    public static ChargePointTransactionHistory buildTransactionHistory(ChargePoint chargePoint, List<Transaction> transactionList) {

        ChargePointTransactionHistory chargePointTransactionHistory = new ChargePointTransactionHistory();
        chargePointTransactionHistory.serialNumber = chargePoint.getSerialNumber();
        chargePointTransactionHistory.lastUpdated = chargePoint.getLastUpdated().atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
        chargePointTransactionHistory.lastHeartBeat = chargePoint.getLastHeartBeat().atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
        chargePointTransactionHistory.transactions = new ArrayList<>();
        for(Transaction transaction : transactionList) {
            chargePointTransactionHistory.transactions.add(new TransactionSummary(
                    transaction.getConnector().getConnectorId(),
                    transaction.getIdTag(),
                    transaction.getStartTime().atOffset(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_INSTANT),
                    transaction.getEndTime().atOffset(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_INSTANT),
                    transaction.getMeterStart(),
                    transaction.getMeterStop()
            ));
        }
        return chargePointTransactionHistory;
    }
}
