package com.kacho.my_ocpp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "socket_session")
public class SocketSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name="serial_number", referencedColumnName="serial_number", unique = true)
    private ChargePoint chargePoint;

    @Column(name="session_id",  unique = true, nullable = false)
    private String sessionId;

    public SocketSession(ChargePoint chargePoint, String sessionId) {
        this.chargePoint = chargePoint;
        this.sessionId = sessionId;
    }
}
