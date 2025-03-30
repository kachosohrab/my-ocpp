package com.kacho.my_ocpp.repository;

import com.kacho.my_ocpp.model.ChargePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChargePointRepository extends JpaRepository<ChargePoint, Long> {

    Optional<ChargePoint> findBySerialNumber(String serialNumber);
}
