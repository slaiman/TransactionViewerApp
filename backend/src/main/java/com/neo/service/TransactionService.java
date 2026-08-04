package com.neo.service;

import com.neo.dto.CreateTransactionRequest;
import com.neo.dto.SortDirection;
import com.neo.dto.TransactionFilter;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionPersistenceService transactionPersistenceService;

    /**
     * Per-transaction-id locks guarding the reverse operation's check-then-act
     * sequence (read status, verify it's POSTED, then set it to REVERSED).
     * computeIfAbsent atomically gets-or-creates the lock object for a given
     * id, and different ids get independent lock objects — so reversing
     * txn-A and txn-B concurrently is still fully parallel. Only concurrent
     * attempts to reverse the *same* id are forced to serialize.
     *
     * This map's size is bounded by the number of distinct transaction ids
     * that have ever had a reversal attempted, which itself can't exceed the
     * total number of transactions in the system — so it does not grow
     * without bound over the app's lifetime.
     */
    private final ConcurrentHashMap<String, Object> reversalLocks = new ConcurrentHashMap<>();

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
    public List<Transaction> getTransactions(String accountId, TransactionFilter transactionFilter) {

        log.info(
                "Retrieving transactions accountId={} transactionFilter={}",
                accountId,
                transactionFilter
        );

        Stream<Transaction> stream =
                transactionRepository
                        .findByAccountId(accountId)
                        .stream();

        List<Transaction> result = null;

        if (transactionFilter != null) {

            // Define base comparator based on sortBy field
            Comparator<Transaction> comparator = switch (transactionFilter.sortBy()) {
                case AMOUNT -> Comparator.comparing(Transaction::getAmount);
                case DATE -> Comparator.comparing(Transaction::getDate);
            };

            // Reverse if requested
            if (transactionFilter.sortDirection() == SortDirection.DESC) {
                comparator = comparator.reversed();
            }

            stream = stream.filter(t ->
                            // Status filter (ignore if null)
                            (transactionFilter.status() == null || t.getStatus() == transactionFilter.status())

                            // Merchant filter (ignore if null/blank, case-insensitive partial match)
                            && (transactionFilter.merchant() == null || transactionFilter.merchant().isBlank()
                            || (t.getMerchantName() != null && t.getMerchantName().toLowerCase().contains(transactionFilter.merchant().toLowerCase())))

                            // Date From filter (ignore if null) -> t.getDate() >= dateFrom
                            && (transactionFilter.dateFrom() == null || !t.getDate().isBefore(transactionFilter.dateFrom()))

                            // Date To filter (ignore if null) -> t.getDate() <= dateTo
                            && (transactionFilter.dateTo() == null || !t.getDate().isAfter(transactionFilter.dateTo()))

                            // Amount Min filter (ignore if null) -> t.getAmount() >= amountMin
                            && (transactionFilter.amountMin() == null || t.getAmount().compareTo(transactionFilter.amountMin()) >= 0)

                            // Amount Max filter (ignore if null) -> t.getAmount() <= amountMax
                            && (transactionFilter.amountMax() == null || t.getAmount().compareTo(transactionFilter.amountMax()) <= 0)
            );
            // Apply sorting and collect
            result = stream
                    .sorted(comparator)
                    .toList();
        }
        else result = stream.toList();


        log.info(
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

        Object lock = reversalLocks.computeIfAbsent(transactionId, id -> new Object());

        synchronized (lock) {
            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new TransactionNotFoundException(transactionId));

            TransactionStatus oldStatus = transaction.getStatus();

            if (oldStatus != TransactionStatus.POSTED) {

                log.info(
                        "Cannot reverse transaction id={} currentStatus={}",
                        transactionId,
                        oldStatus
                );

                throw new InvalidTransactionStateException(
                        "Only transactions with status POSTED can be reversed. Current status: "
                                + oldStatus);
            }

            transaction.setStatus(TransactionStatus.REVERSED);
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
}
