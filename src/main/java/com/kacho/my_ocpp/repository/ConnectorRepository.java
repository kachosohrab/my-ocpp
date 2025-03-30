package com.kacho.my_ocpp.repository;

import com.kacho.my_ocpp.model.Connector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConnectorRepository extends JpaRepository<Connector, Long> {
    Optional<Connector> findByConnectorIdAndChargePointSerialNumber(int connectorId, String serialNumber);
}
