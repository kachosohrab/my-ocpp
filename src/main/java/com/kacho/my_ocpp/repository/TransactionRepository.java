package com.kacho.my_ocpp.repository;

import com.kacho.my_ocpp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByIdTagAndEndTimeIsNull(String idTag);

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.chargePoint.serialNumber = :serialNumber " +
            "AND t.startTime BETWEEN :start AND :end")
    List<Transaction> getTransactionalHistory(String serialNumber, Instant start, Instant end);
}
