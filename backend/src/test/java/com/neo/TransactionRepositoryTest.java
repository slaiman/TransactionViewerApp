package com.neo;

import com.neo.model.Transaction;
import com.neo.model.TransactionStatus;
import com.neo.repository.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRepositoryTest {

    private final TransactionRepository repository = new TransactionRepository();

    private static Transaction transaction(String id, String accountId, TransactionStatus status) {
        return Transaction.builder()
                .id(id)
                .accountId(accountId)
                .date(LocalDate.of(2026, 7, 1))
                .merchantName("Test Merchant")
                .amount(BigDecimal.TEN)
                .status(status)
                .build();
    }

    @Test
    void load_indexesAllTransactionsByIdAndAccount() {
        repository.load(List.of(
                transaction("txn-1", "acc-001", TransactionStatus.POSTED),
                transaction("txn-2", "acc-001", TransactionStatus.PENDING),
                transaction("txn-3", "acc-002", TransactionStatus.POSTED)
        ));

        assertThat(repository.findAllTransactions()).hasSize(3);
        assertThat(repository.findByAccountId("acc-001")).extracting(Transaction::getId)
                .containsExactlyInAnyOrder("txn-1", "txn-2");
        assertThat(repository.findByAccountId("acc-002")).extracting(Transaction::getId)
                .containsExactly("txn-3");
    }

    @Test
    void findById_returnsPresentTransaction() {
        repository.save(transaction("txn-1", "acc-001", TransactionStatus.POSTED));

        assertThat(repository.findById("txn-1")).isPresent()
                .get().extracting(Transaction::getId).isEqualTo("txn-1");
    }

    @Test
    void findById_returnsEmptyForUnknownId() {
        assertThat(repository.findById("does-not-exist")).isEmpty();
    }

    @Test
    void findByAccountId_returnsEmptyListForUnknownAccount() {
        assertThat(repository.findByAccountId("no-such-account")).isEmpty();
    }

    @Test
    void findAllAccounts_returnsDistinctAccountsSortedAlphabetically() {
        repository.load(List.of(
                transaction("txn-1", "acc-003", TransactionStatus.POSTED),
                transaction("txn-2", "acc-001", TransactionStatus.POSTED),
                transaction("txn-3", "acc-002", TransactionStatus.POSTED),
                transaction("txn-4", "acc-001", TransactionStatus.PENDING) // same account again
        ));

        assertThat(repository.findAllAccounts()).containsExactly("acc-001", "acc-002", "acc-003");
    }

    @Test
    void save_addsNewTransactionAndIndexesItByAccount() {
        Transaction saved = repository.save(transaction("txn-1", "acc-001", TransactionStatus.PENDING));

        assertThat(saved.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(repository.findById("txn-1")).isPresent();
        assertThat(repository.findByAccountId("acc-001")).hasSize(1);
    }

    @Test
    void save_onExistingIdOverwritesInPlace_doesNotDuplicateInAccountIndex() {
        Transaction original = transaction("txn-1", "acc-001", TransactionStatus.POSTED);
        repository.save(original);

        original.setStatus(TransactionStatus.REVERSED);
        repository.save(original);

        assertThat(repository.findAllTransactions()).hasSize(1);
        assertThat(repository.findByAccountId("acc-001")).hasSize(1);
        assertThat(repository.findById("txn-1")).get()
                .extracting(Transaction::getStatus).isEqualTo(TransactionStatus.REVERSED);
    }
}