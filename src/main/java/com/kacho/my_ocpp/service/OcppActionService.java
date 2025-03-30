package com.kacho.my_ocpp.service;

import com.kacho.my_ocpp.dto.OcppRequest;
import com.kacho.my_ocpp.dto.OcppResponse;
import com.kacho.my_ocpp.dto.impl.*;
import com.kacho.my_ocpp.enums.OcppAction;
import com.kacho.my_ocpp.exception.ApiException;
import com.kacho.my_ocpp.exception.CallErrorException;
import com.kacho.my_ocpp.model.ChargePoint;
import com.kacho.my_ocpp.model.Connector;
import com.kacho.my_ocpp.model.SocketSession;
import com.kacho.my_ocpp.model.Transaction;
import com.kacho.my_ocpp.repository.ChargePointRepository;
import com.kacho.my_ocpp.repository.ConnectorRepository;
import com.kacho.my_ocpp.repository.SocketSessionRepository;
import com.kacho.my_ocpp.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OcppActionService {

    private final ChargePointRepository chargePointRepository;
    private final ConnectorRepository connectorRepository;
    private final TransactionRepository transactionRepository;
    private final SocketSessionRepository socketSessionRepository;
    private final RedisService redisService;

    private final Logger logger = LoggerFactory.getLogger(OcppActionService.class);

    public OcppActionService(ChargePointRepository chargePointRepository,
                             ConnectorRepository connectorRepository,
                             TransactionRepository transactionRepository,
                             SocketSessionRepository socketSessionRepository,
                             RedisService redisService) {
        this.chargePointRepository = chargePointRepository;
        this.connectorRepository = connectorRepository;
        this.transactionRepository = transactionRepository;
        this.socketSessionRepository = socketSessionRepository;
        this.redisService = redisService;
    }

    public void afterConnectionClosed(String sessionUUID) {
        redisService.deleteSession(sessionUUID);
        Optional<SocketSession> socketSessionOptional = socketSessionRepository.findBySessionId(sessionUUID);
        if(socketSessionOptional.isEmpty()) return;
        SocketSession socketSession = socketSessionOptional.get();
        Optional<ChargePoint> chargePointOptional = chargePointRepository.findBySerialNumber(
                socketSession.getChargePoint().getSerialNumber());
        if(chargePointOptional.isEmpty()) return;
        ChargePoint chargePoint = chargePointOptional.get();
        chargePoint.setStatus(ChargePoint.Status.FAULTED.name());
        chargePointRepository.save(chargePoint);
        socketSessionRepository.delete(socketSession);
    }

    public OcppResponse processAction(String sessionUUID, OcppRequest request) {
        if(!redisService.sessionExists(sessionUUID)
                && !request.getOcppAction().equals(OcppAction.BOOT_NOTIFICATION)) {
            logger.error("Error: Session Not Available in Redis For: " + sessionUUID);
            throw new CallErrorException("Provided Session Does Not Exist");
        }
        return switch (request.getOcppAction()) {
            case BOOT_NOTIFICATION -> processBootNotification(sessionUUID, (BootNotificationRequest) request);
            case HEARTBEAT -> processHeartBeat(sessionUUID, (HeartBeatRequest) request);
            case START_TRANSACTION -> processStartTransaction(sessionUUID, (StartTransactionRequest) request);
            case STOP_TRANSACTION -> processStopTransaction(sessionUUID, (StopTransactionRequest) request);
            case STATUS_NOTIFICATION -> processStatusNotification(sessionUUID, (StatusNotificationRequest) request);
            default -> throw new CallErrorException("Action Type Not Supported By CALL");
        };
    }

    private BootNotificationResponse processBootNotification(String sessionUUID, BootNotificationRequest bootNotificationRequest) {
        Optional<ChargePoint> chargePointOptional = chargePointRepository.findBySerialNumber(bootNotificationRequest.getChargePointSerialNumber());
        ChargePoint chargePoint = chargePointOptional.orElse(ChargePoint.chargePointFromBootNotificationRequest(bootNotificationRequest));
        chargePoint.setStatus(ChargePoint.Status.AVAILABLE.name());
        Instant now = Instant.now();
        chargePoint.setLastHeartBeat(now);
        chargePointRepository.save(chargePoint);
        Optional<SocketSession> socketSessionOptional = socketSessionRepository.findByChargePoint(chargePoint);
        SocketSession socketSession = socketSessionOptional.orElse(new SocketSession(chargePoint, sessionUUID));
        socketSessionRepository.save(socketSession);
        redisService.saveSession(sessionUUID, chargePoint.getSerialNumber(), 5);
        String formattedNow = now.atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
        return new BootNotificationResponse("ACCEPTED", formattedNow, 30);
    }

    private HeartBeatResponse processHeartBeat(String sessionUUID, HeartBeatRequest heartBeatRequest) {
        String serialNumber = redisService.getSerialNumber(sessionUUID);
        Optional<ChargePoint> chargePointOptional = chargePointRepository.findBySerialNumber(serialNumber);
        if(chargePointOptional.isEmpty()) {
            logger.error(String.format("ChargePoint Not Available in DB for %s", serialNumber));
            throw new CallErrorException("Session Id and Charge Point Mismatch");
        }
        ChargePoint chargePoint = chargePointOptional.get();
        Instant now = Instant.now();
        String formattedNow = now.atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
        chargePoint.setLastHeartBeat(now);
        chargePointRepository.save(chargePoint);
        redisService.refreshSessionTTL(sessionUUID,5);
        return new HeartBeatResponse(formattedNow);
    }

    private StatusNotificationResponse processStatusNotification(String sessionUUID, StatusNotificationRequest statusNotificationRequest) {
        String serialNumber = redisService.getSerialNumber(sessionUUID);
        Optional<Connector> connectorOpt = connectorRepository.findByConnectorIdAndChargePointSerialNumber(statusNotificationRequest.getConnectorId(),
                redisService.getSerialNumber(sessionUUID));
        Connector connector;
        if (connectorOpt.isEmpty()) {
            if(statusNotificationRequest.getStatus().equals("Available")) {
                Optional<ChargePoint> chargePointOptional = chargePointRepository.findBySerialNumber(serialNumber);
                if(chargePointOptional.isEmpty()) {
                    logger.error(String.format("Charge Point With Serial Number %s Not Available For New Connector", serialNumber));
                    throw new CallErrorException(String.format("Charge Point With Serial Number %s Not Available For New Connector", serialNumber));
                }
                ChargePoint chargePoint = chargePointOptional.get();
                connector = new Connector();
                connector.setStatus("Available");
                connector.setConnectorId(statusNotificationRequest.getConnectorId());
                connector.setChargePoint(chargePoint);
                connector.setErrorCode(statusNotificationRequest.getErrorCode());
            } else {
                logger.error(String.format("Connector Not Available in DB For Serial Number: %s and Connector Id: %s", serialNumber,
                        statusNotificationRequest.getConnectorId()));
                throw new CallErrorException("Connector Not Found");
            }
        } else {
            connector = connectorOpt.orElse(new Connector());
            // Update connector status
            connector.setStatus(statusNotificationRequest.getStatus());
            connector.setErrorCode(statusNotificationRequest.getErrorCode());
        }


        connectorRepository.save(connector);

        return new StatusNotificationResponse("Accepted");
    }


    private StartTransactionResponse processStartTransaction(String sessionUUID, StartTransactionRequest startTransactionRequest) {
        String serialNumber = redisService.getSerialNumber(sessionUUID);
        Optional<ChargePoint> chargePointOptional = chargePointRepository.findBySerialNumber(serialNumber);
        if(chargePointOptional.isEmpty()) {
            logger.error(String.format("ChargePoint Not Available in DB for %s", serialNumber));
            throw new CallErrorException("Session Id and Charge Point Mismatch");
        }

        ChargePoint chargePoint = chargePointOptional.get();
        Optional<Connector>  connectorOptional = connectorRepository.findByConnectorIdAndChargePointSerialNumber(
                startTransactionRequest.getConnectorId(), chargePoint.getSerialNumber());
        if (connectorOptional.isEmpty()) {
            logger.error(String.format("Connector Not Available in DB For Serial Number: %s and Connector Id: %s",
                    chargePoint.getSerialNumber(),
                    startTransactionRequest.getConnectorId()));
            throw new CallErrorException("Connector Not Found");
        }
        Connector connector = connectorOptional.get();
        if (!"Available".equalsIgnoreCase(connector.getStatus())) {
            logger.error("Connector is not available. The current status is: " + connector.getStatus());
            throw new CallErrorException("Connector is not available. The current status is: " + connector.getStatus());
        }
        connector.setStatus("Charging");
        connectorRepository.save(connector);
        Transaction transaction = new Transaction();
        transaction.setChargePoint(chargePoint);
        transaction.setConnector(connector);
        transaction.setIdTag(startTransactionRequest.getIdTag());
        transaction.setStartTime(Instant.parse(startTransactionRequest.getTimestamp()));
        transaction.setMeterStart(startTransactionRequest.getMeterStart());
        transaction.setLastUpdated(Instant.parse(startTransactionRequest.getTimestamp()));
        transactionRepository.save(transaction);
        return new StartTransactionResponse(transaction.getId().toString(), "Accepted");
    }

    private StopTransactionResponse processStopTransaction(String sessionUUID, StopTransactionRequest stopTransactionRequest) {
        UUID uuid;
        try {
            uuid = UUID.fromString(stopTransactionRequest.getTransactionId());
        } catch (IllegalArgumentException e) {
            throw new CallErrorException("Invalid Transaction ID Format");
        }
        Optional<Transaction> optionalTransaction = transactionRepository.findById(uuid);
        if (optionalTransaction.isEmpty()) {
            logger.error(String.format("Transaction Not Found For Transaction Id: %s", stopTransactionRequest.getTransactionId()));
            throw new CallErrorException("Transaction Not Found");
        }
        Transaction transaction = optionalTransaction.get();
        if (transaction.getEndTime() != null) {
            logger.error(String.format("Transaction has already stopped for transaction Id: %s", stopTransactionRequest.getTransactionId()));
            throw new CallErrorException("Transaction has already been stopped");
        }
        String serialNumber = redisService.getSerialNumber(sessionUUID);
        Optional<Connector> connectorOptional = connectorRepository
                .findByConnectorIdAndChargePointSerialNumber(transaction.getConnector().getConnectorId(),
                        serialNumber);
        if(connectorOptional.isPresent()) {
            Connector connector = connectorOptional.get();
            connector.setStatus("Available");
            connectorRepository.save(connector);
        }
        // Set the end time and meter stop value
        transaction.setEndTime(Instant.parse(stopTransactionRequest.getTimestamp()));
        transaction.setMeterStop(stopTransactionRequest.getMeterStop());
        transaction.setLastUpdated(Instant.parse(stopTransactionRequest.getTimestamp()));

        transactionRepository.save(transaction);

        return new StopTransactionResponse("Accepted");
    }

    public ChargePointTransactionHistory getChargePointTransactionHistory(String serialNumber, String startTime, String endTime) {
        Optional<ChargePoint> chargePointOptional = chargePointRepository.findBySerialNumber(serialNumber);
        if(chargePointOptional.isEmpty()) {
            throw new ApiException("Charge Point Not Available For Serial Number: " + serialNumber,
                    HttpStatus.BAD_REQUEST);
        }
        ChargePoint chargePoint = chargePointOptional.get();
        Instant startInstance = Instant.parse(startTime), endInstance = Instant.parse(endTime);
        if (startInstance.isAfter(endInstance)) {
            throw new ApiException("Start time cannot be after end time", HttpStatus.BAD_REQUEST);
        }
        List<Transaction> transactions = transactionRepository.getTransactionalHistory(chargePoint.getSerialNumber(),
                Instant.parse(startTime), Instant.parse(endTime));
        return ChargePointTransactionHistory.buildTransactionHistory(chargePoint, transactions);
    }

}
