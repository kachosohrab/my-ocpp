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
@Table(name = "connector",
    uniqueConstraints = @UniqueConstraint(columnNames = {"connector_id", "serial_number"}))
public class Connector {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "serial_number", referencedColumnName = "serial_number", nullable = false)
    private ChargePoint chargePoint;

    @Column(name="connector_id", nullable = false)
    private int connectorId; // Unique per charge point

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String errorCode;

    @Column(nullable = false)
    private Instant lastUpdated;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.lastUpdated = Instant.now();
    }
}
