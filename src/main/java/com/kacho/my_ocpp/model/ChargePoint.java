package com.kacho.my_ocpp.model;

import com.kacho.my_ocpp.dto.impl.BootNotificationRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name="charge_point")
public class ChargePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="serial_number", unique = true, nullable = false)
    private String serialNumber;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String vendor;

    @Column(nullable = false)
    private String firmwareVersion;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant lastHeartBeat;

    private Instant lastUpdated;

    @PreUpdate
    public void onUpdate() {
        lastUpdated = Instant.now();
    }

    @PrePersist
    public void onCreate() {
        lastUpdated = Instant.now();
    }

    private ChargePoint(){}

    public static ChargePoint chargePointFromBootNotificationRequest(BootNotificationRequest bootNotificationRequest) {
        ChargePoint chargePoint = new ChargePoint();
        chargePoint.serialNumber = bootNotificationRequest.getChargePointSerialNumber();
        chargePoint.model = bootNotificationRequest.getChargePointModel();
        chargePoint.firmwareVersion = bootNotificationRequest.getFirmwareVersion();
        chargePoint.lastUpdated = Instant.now();
        chargePoint.lastHeartBeat = Instant.now();
        chargePoint.status = Status.AVAILABLE.name();
        chargePoint.vendor = bootNotificationRequest.getChargePointVendor();
        return chargePoint;
    }

    public enum Status {
        AVAILABLE,
        FAULTED,
        CHARGING;
    }

}
