package com.neo.service;

import com.neo.dto.CreateTransactionRequest;
import com.neo.exception.InvalidTransactionStateException;
import com.neo.exception.TransactionNotFoundException;
import com.neo.model.Transaction;
import com.neo.model.TransactionStatus;
import com.neo.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionPersistenceService transactionPersistenceService;
    private static final Logger auditLogger = LoggerFactory.getLogger("TRANSACTION_LOGGER");

    public TransactionService(TransactionRepository transactionRepository, TransactionPersistenceService transactionPersistenceService) {
        this.transactionRepository = transactionRepository;
        this.transactionPersistenceService = transactionPersistenceService;
    }

    /**
     * Retrieve all Account IDs.
     */
    public List<String> getAccounts() {
        return transactionRepository.findAllAccounts();
    }

    /**
     * Retrieve all transactions for a given account, optionally filtered by status and sorted by date of transaction.
     */
    public List<Transaction> getTransactions(String accountId, TransactionStatus statusFilter) {

        log.debug(
                "Retrieving transactions accountId={} statusFilter={}",
                accountId,
                statusFilter
        );

        Stream<Transaction> stream =
                transactionRepository
                        .findByAccountId(accountId)
                        .stream();

        if (statusFilter != null) {
            stream = stream.filter(t -> t.getStatus() == statusFilter);
        }

        List<Transaction> result = stream
                .sorted(
                        Comparator.comparing(Transaction::getDate)
                                .reversed()
                )
                .toList();

        log.debug(
                "Retrieved {} transactions for accountId={}",
                result.size(),
                accountId
        );

        return result;
    }

    /**
     * Create a new transaction simulating a purchase. New transactions always
     * start life as PENDING, mirroring how card authorizations behave before
     * they settle.
     */
    public Transaction createTransaction(CreateTransactionRequest request) {

        log.info(
                "Creating new transaction for accountId={}",
                request.accountId()
        );

        Transaction transaction = Transaction.builder()
                .id("txn-" + UUID.randomUUID())
                .accountId(request.accountId())
                .date(request.date() != null ? request.date() : LocalDate.now())
                .merchantName(request.merchantName())
                .amount(request.amount())
                .status(TransactionStatus.PENDING)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        transactionPersistenceService.persist(saved);

        auditLogger.info(
                "CREATE_TRANSACTION id={} accountId={} merchant={} amount={} status={}",
                saved.getId(),
                saved.getAccountId(),
                saved.getMerchantName(),
                saved.getAmount(),
                saved.getStatus()
        );

        return saved;
    }

    /**
     * Reverse a transaction. Only transactions currently in POSTED status may
     * be reversed.
     */
    public Transaction reverseTransaction(String transactionId) {

        log.info(
                "Reverse request received transactionId={}",
                transactionId
        );

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        TransactionStatus oldStatus =
                transaction.getStatus();

        if (transaction.getStatus() != TransactionStatus.POSTED) {

            log.warn(
                    "Cannot reverse transaction id={} currentStatus={}",
                    transactionId,
                    oldStatus
            );

            throw new InvalidTransactionStateException(
                    "Only transactions with status POSTED can be reversed. Current status: "
                            + transaction.getStatus());
        }
        transaction.setStatus(TransactionStatus.REVERSED);
        transactionRepository.update(transaction);
        transactionPersistenceService.persist(transaction);

        auditLogger.info(
                "REVERSE_TRANSACTION id={} accountId={} oldStatus={} newStatus={}",
                transaction.getId(),
                transaction.getAccountId(),
                oldStatus,
                transaction.getStatus()
        );

        return transaction;
    }
}
