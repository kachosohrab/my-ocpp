package com.kacho.my_ocpp.repository;

import com.kacho.my_ocpp.model.ChargePoint;
import com.kacho.my_ocpp.model.SocketSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocketSessionRepository extends JpaRepository<SocketSession, Long> {

    Optional<SocketSession> findBySessionId(String sessionId);
    Optional<SocketSession> findByChargePoint(ChargePoint chargePoint);
}
