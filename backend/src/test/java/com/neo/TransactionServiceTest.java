package com.neo;

import com.neo.dto.CreateTransactionRequest;
import com.neo.dto.PageResponse;
import com.neo.dto.TransactionFilter;
import com.neo.exception.InvalidTransactionStateException;
import com.neo.exception.TransactionNotFoundException;
import com.neo.model.Transaction;
import com.neo.model.TransactionStatus;
import com.neo.repository.AccountRepository;
import com.neo.repository.TransactionRepository;
import com.neo.service.AuditService;
import com.neo.service.TransactionPersistenceService;
import com.neo.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepositoryMock;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private TransactionPersistenceService transactionPersistenceServiceMock;

    private TransactionService service;

    @BeforeEach
    void setUp() {
        org.mockito.MockitoAnnotations.openMocks(this);
        service = new TransactionService(transactionRepositoryMock, accountRepository,transactionPersistenceServiceMock,auditService);
    }

    private static Transaction transaction(String id, String accountId, TransactionStatus status, LocalDate date) {
        return Transaction.builder()
                .id(id)
                .accountId(accountId)
                .date(date)
                .merchantName("Test Merchant")
                .amount(BigDecimal.TEN)
                .status(status)
                .build();
    }

    // ---- getAccounts ----------------------------------------------------

    @Test
    void getAccounts_delegatesToRepository() {
        when(transactionRepositoryMock.findAllAccounts()).thenReturn(List.of("acc-001", "acc-002"));

        assertThat(service.getAccounts()).containsExactly("acc-001", "acc-002");
    }

    // ---- getTransactions --------------------------------------------------

    @Test
    void getTransactions_sortsByDateDescending() {
        Transaction older = transaction("txn-1", "acc-001", TransactionStatus.POSTED, LocalDate.of(2026, 1, 1));
        Transaction newer = transaction("txn-2", "acc-001", TransactionStatus.POSTED, LocalDate.of(2026, 6, 1));
        when(transactionRepositoryMock.findByAccountId("acc-001")).thenReturn(List.of(older, newer));

        PageResponse<Transaction> result = service.getTransactions("acc-001", null, null);

        assertThat(result.content()).extracting(Transaction::getId).containsExactly("txn-2", "txn-1");
    }

    @Test
    void getTransactions_filtersByStatusWhenProvided() {
        Transaction posted = transaction("txn-1", "acc-001", TransactionStatus.POSTED, LocalDate.now());
        Transaction pending = transaction("txn-2", "acc-001", TransactionStatus.PENDING, LocalDate.now());
        when(transactionRepositoryMock.findByAccountId("acc-001")).thenReturn(List.of(posted, pending));

        PageResponse<Transaction> result = service.getTransactions("acc-001", TransactionFilter.builder().status(TransactionStatus.POSTED).build(), null);

        assertThat(result.content()).containsExactly(posted);
    }

    // ---- createTransaction ------------------------------------------------

    @Test
    void createTransaction_alwaysStartsAsPending() {
        CreateTransactionRequest request = new CreateTransactionRequest("acc-001", null, "Amazon.com", BigDecimal.valueOf(99.99));
        when(transactionRepositoryMock.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction created = service.createTransaction(request);

        assertThat(created.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(created.getId()).startsWith("txn-");
        assertThat(created.getDate()).isEqualTo(LocalDate.now());
        verify(transactionPersistenceServiceMock).persist(created);
    }

    @Test
    void createTransaction_usesProvidedDateWhenPresent() {
        LocalDate explicitDate = LocalDate.of(2026, 5, 1);
        CreateTransactionRequest request = new CreateTransactionRequest("acc-001", explicitDate, "Costco", BigDecimal.TEN);
        when(transactionRepositoryMock.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction created = service.createTransaction(request);

        assertThat(created.getDate()).isEqualTo(explicitDate);
    }

    // ---- confirmTransaction (business rules) -------------------------------

    @Test
    void confirmTransaction_whenPending_setsStatusToPostedAndPersists() {
        Transaction pending = transaction("txn-1", "acc-001", TransactionStatus.PENDING, LocalDate.now());
        when(transactionRepositoryMock.findById("txn-1")).thenReturn(Optional.of(pending));

        Transaction result = service.confirmTransaction("txn-1");

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.POSTED);
        verify(transactionPersistenceServiceMock).persist(pending);
    }

    @Test
    void confirmTransaction_whenAlreadyPosted_throwsInvalidTransactionStateException() {
        Transaction posted = transaction("txn-2", "acc-001", TransactionStatus.POSTED, LocalDate.now());
        when(transactionRepositoryMock.findById("txn-2")).thenReturn(Optional.of(posted));

        assertThatThrownBy(() -> service.confirmTransaction("txn-2"))
                .isInstanceOf(InvalidTransactionStateException.class)
                .hasMessageContaining("PENDING");

        verify(transactionPersistenceServiceMock, never()).persist(any());
    }

    @Test
    void confirmTransaction_whenReversed_throwsInvalidTransactionStateException() {
        Transaction reversed = transaction("txn-3", "acc-001", TransactionStatus.REVERSED, LocalDate.now());
        when(transactionRepositoryMock.findById("txn-3")).thenReturn(Optional.of(reversed));

        assertThatThrownBy(() -> service.confirmTransaction("txn-3"))
                .isInstanceOf(InvalidTransactionStateException.class);

        verify(transactionPersistenceServiceMock, never()).persist(any());
    }

    @Test
    void confirmTransaction_whenNotFound_throwsTransactionNotFoundException() {
        when(transactionRepositoryMock.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmTransaction("missing"))
                .isInstanceOf(TransactionNotFoundException.class);

        verify(transactionPersistenceServiceMock, never()).persist(any());
    }

    @Test
    void confirmTransaction_underConcurrentAttempts_onlyOneSucceeds() throws InterruptedException {
        TransactionRepository realRepository = new TransactionRepository();
        realRepository.save(transaction("txn-race", "acc-001", TransactionStatus.PENDING, LocalDate.now()));

        TransactionService realService = new TransactionService(realRepository, accountRepository,transactionPersistenceServiceMock, auditService);

        int threadCount = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startingGate = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                try {
                    startingGate.await();
                    realService.confirmTransaction("txn-race");
                    successCount.incrementAndGet();
                } catch (InvalidTransactionStateException e) {
                    conflictCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finished.countDown();
                }
            });
        }

        startingGate.countDown();
        boolean completedInTime = finished.await(10, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(completedInTime).isTrue();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(threadCount - 1);
        assertThat(realRepository.findById("txn-race")).get()
                .extracting(Transaction::getStatus).isEqualTo(TransactionStatus.POSTED);
    }

    // ---- reverseTransaction (business rules) -------------------------------

    @Test
    void reverseTransaction_whenPosted_setsStatusToReversedAndPersists() {
        Transaction posted = transaction("txn-1", "acc-001", TransactionStatus.POSTED, LocalDate.now());
        when(transactionRepositoryMock.findById("txn-1")).thenReturn(Optional.of(posted));

        Transaction result = service.reverseTransaction("txn-1");

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.REVERSED);
        verify(transactionPersistenceServiceMock).persist(posted);
    }

    @Test
    void reverseTransaction_whenPending_throwsInvalidTransactionStateException() {
        Transaction pending = transaction("txn-2", "acc-001", TransactionStatus.PENDING, LocalDate.now());
        when(transactionRepositoryMock.findById("txn-2")).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> service.reverseTransaction("txn-2"))
                .isInstanceOf(InvalidTransactionStateException.class)
                .hasMessageContaining("POSTED");

        verify(transactionPersistenceServiceMock, never()).persist(any());
    }

    @Test
    void reverseTransaction_whenAlreadyReversed_throwsInvalidTransactionStateException() {
        Transaction reversed = transaction("txn-3", "acc-001", TransactionStatus.REVERSED, LocalDate.now());
        when(transactionRepositoryMock.findById("txn-3")).thenReturn(Optional.of(reversed));

        assertThatThrownBy(() -> service.reverseTransaction("txn-3"))
                .isInstanceOf(InvalidTransactionStateException.class);

        verify(transactionPersistenceServiceMock, never()).persist(any());
    }

    @Test
    void reverseTransaction_whenNotFound_throwsTransactionNotFoundException() {
        when(transactionRepositoryMock.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reverseTransaction("missing"))
                .isInstanceOf(TransactionNotFoundException.class);

        verify(transactionPersistenceServiceMock, never()).persist(any());
    }

    // ---- reverseTransaction (concurrency regression test) -----------------

    /**
     * Regression test for the check-then-act race in reverseTransaction: two
     * concurrent reversal attempts on the *same* transaction id must not both
     * succeed. This test uses a real TransactionRepository (not a mock) so
     * the shared mutable Transaction instance and the per-id lock in
     * TransactionService are both genuinely exercised, and a CountDownLatch
     * "starting gate" to align all threads' start times and maximize actual
     * contention rather than relying on incidental thread scheduling.
     */
    @Test
    void reverseTransaction_underConcurrentAttempts_onlyOneSucceeds() throws InterruptedException {
        TransactionRepository realRepository = new TransactionRepository();
        realRepository.save(transaction("txn-race", "acc-001", TransactionStatus.POSTED, LocalDate.now()));

        TransactionService realService = new TransactionService(realRepository, accountRepository,transactionPersistenceServiceMock, auditService);

        int threadCount = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startingGate = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                try {
                    startingGate.await();
                    realService.reverseTransaction("txn-race");
                    successCount.incrementAndGet();
                } catch (InvalidTransactionStateException e) {
                    conflictCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finished.countDown();
                }
            });
        }

        startingGate.countDown(); // release all threads at once
        boolean completedInTime = finished.await(10, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(completedInTime).isTrue();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(threadCount - 1);
        assertThat(realRepository.findById("txn-race")).get()
                .extracting(Transaction::getStatus).isEqualTo(TransactionStatus.REVERSED);
        verify(transactionPersistenceServiceMock, times(1)).persist(any(Transaction.class));
    }

    @Test
    void reverseTransaction_concurrentAttemptsOnDifferentIds_bothSucceedIndependently() throws InterruptedException {
        TransactionRepository realRepository = new TransactionRepository();
        realRepository.save(transaction("txn-a", "acc-001", TransactionStatus.POSTED, LocalDate.now()));
        realRepository.save(transaction("txn-b", "acc-001", TransactionStatus.POSTED, LocalDate.now()));

        TransactionService realService = new TransactionService(realRepository, accountRepository,transactionPersistenceServiceMock, auditService);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch finished = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger();

        for (String id : List.of("txn-a", "txn-b")) {
            pool.submit(() -> {
                try {
                    realService.reverseTransaction(id);
                    successCount.incrementAndGet();
                } finally {
                    finished.countDown();
                }
            });
        }

        finished.await(10, TimeUnit.SECONDS);
        pool.shutdown();

        // different ids get different lock objects, so both succeed independently
        assertThat(successCount.get()).isEqualTo(2);
    }
}