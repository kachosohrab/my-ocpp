package com.kacho.my_ocpp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "transaction")
public class Transaction {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "serial_number", referencedColumnName = "serial_number", nullable = false)
    private ChargePoint chargePoint;

    @ManyToOne
    @JoinColumn(name = "connector_id", referencedColumnName = "id", nullable = false)
    private Connector connector;

    @Column(nullable = false)
    private String idTag;

    @Column(nullable = false)
    private Instant startTime;

    private Instant endTime;

    @Column(nullable = false)
    private int meterStart;

    private int meterStop;

    @Column(nullable = false)
    private Instant lastUpdated;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.lastUpdated = Instant.now();
    }
}
