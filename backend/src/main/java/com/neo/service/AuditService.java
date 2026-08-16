package com.neo.service;

import com.neo.model.AuditEvent;
import com.neo.model.Transaction;
import com.neo.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;
    private final AuditPersistenceService auditPersistenceService;

    public void recordTransactionStatusChange(
            Transaction transaction,
            String oldStatus,
            String newStatus) {

        AuditEvent event = AuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .operation("TRANSACTION_STATUS_CHANGE")
                .entityType("TRANSACTION")
                .entityId(transaction.getId())
                .accountId(transaction.getAccountId())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .timestamp(Instant.now())
                .details("Transaction status changed")
                .build();

        auditRepository.save(event);
        auditPersistenceService.enqueue(event);

        log.info(
                "Audit event recorded transactionId={} oldStatus={} newStatus={}",
                transaction.getId(),
                oldStatus,
                newStatus
        );
    }

    public void recordTransactionCreated(Transaction transaction) {

        AuditEvent event = AuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .operation("CREATE_TRANSACTION")
                .entityType("TRANSACTION")
                .entityId(transaction.getId())
                .accountId(transaction.getAccountId())
                .newStatus(transaction.getStatus().name())
                .timestamp(Instant.now())
                .details("Transaction created")
                .build();

        auditRepository.save(event);
        auditPersistenceService.enqueue(event);

        log.info(
                "Audit event recorded for transaction creation id={}",
                transaction.getId()
        );
    }

    public List<AuditEvent> getAllEvents() {
        return auditRepository.findAll();
    }
}