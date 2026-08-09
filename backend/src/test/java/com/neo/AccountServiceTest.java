package com.neo;

import com.neo.dto.AccountBalanceResponse;
import com.neo.dto.AccountResponse;
import com.neo.dto.CreateAccountRequest;
import com.neo.dto.UpdateAccountRequest;
import com.neo.exception.AccountHasTransactionsException;
import com.neo.exception.AccountNotFoundException;
import com.neo.model.Account;
import com.neo.model.AccountStatus;
import com.neo.model.Transaction;
import com.neo.model.TransactionStatus;
import com.neo.repository.AccountRepository;
import com.neo.repository.TransactionRepository;
import com.neo.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepositoryMock;

    @Mock
    private TransactionRepository transactionRepositoryMock;

    private AccountService service;

    @BeforeEach
    void setUp() {
        org.mockito.MockitoAnnotations.openMocks(this);
        service = new AccountService(accountRepositoryMock, transactionRepositoryMock);
    }

    private static Account account(String id, String name, AccountStatus status) {
        return Account.builder()
                .id(id)
                .accountHolderName(name)
                .status(status)
                .createdDate(LocalDate.of(2025, 1, 15))
                .build();
    }

    private static Transaction transaction(String accountId, TransactionStatus status, String amount) {
        return Transaction.builder()
                .id("txn-" + System.nanoTime())
                .accountId(accountId)
                .date(LocalDate.now())
                .merchantName("Merchant")
                .amount(new BigDecimal(amount))
                .status(status)
                .build();
    }

    // ---- balance computation -------------------------------------------

    @Test
    void getBalance_sumsOnlyPostedTransactions() {
        Account acc = account("acc-001", "Alice", AccountStatus.ACTIVE);
        when(accountRepositoryMock.findById("acc-001")).thenReturn(Optional.of(acc));
        when(transactionRepositoryMock.findByAccountId("acc-001")).thenReturn(List.of(
                transaction("acc-001", TransactionStatus.POSTED, "50.00"),
                transaction("acc-001", TransactionStatus.POSTED, "25.50"),
                transaction("acc-001", TransactionStatus.PENDING, "999.00"),
                transaction("acc-001", TransactionStatus.REVERSED, "10.00")
        ));

        AccountBalanceResponse result = service.getBalance("acc-001");

        assertThat(result.balance()).isEqualByComparingTo("75.50");
    }

    @Test
    void getBalance_isZeroWhenNoPostedTransactions() {
        Account acc = account("acc-001", "Alice", AccountStatus.ACTIVE);
        when(accountRepositoryMock.findById("acc-001")).thenReturn(Optional.of(acc));
        when(transactionRepositoryMock.findByAccountId("acc-001")).thenReturn(List.of());

        AccountBalanceResponse result = service.getBalance("acc-001");

        assertThat(result.balance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getBalance_whenAccountNotFound_throwsAccountNotFoundException() {
        when(accountRepositoryMock.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBalance("missing"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    // ---- getAccounts / getAccount ---------------------------------------

    @Test
    void getAccounts_includesComputedBalanceForEach() {
        when(accountRepositoryMock.findAll()).thenReturn(List.of(
                account("acc-001", "Alice", AccountStatus.ACTIVE),
                account("acc-002", "Bob", AccountStatus.ACTIVE)
        ));
        when(transactionRepositoryMock.findByAccountId("acc-001"))
                .thenReturn(List.of(transaction("acc-001", TransactionStatus.POSTED, "10.00")));
        when(transactionRepositoryMock.findByAccountId("acc-002"))
                .thenReturn(List.of());

        List<AccountResponse> result = service.getAccounts();

        assertThat(result).extracting(AccountResponse::id).containsExactly("acc-001", "acc-002");
        assertThat(result.get(0).balance()).isEqualByComparingTo("10.00");
        assertThat(result.get(1).balance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ---- createAccount ----------------------------------------------------

    @Test
    void createAccount_startsActiveWithGeneratedIdAndZeroBalance() {
        CreateAccountRequest request = new CreateAccountRequest("New Holder");
        when(accountRepositoryMock.nextId()).thenReturn("acc-026");
        when(accountRepositoryMock.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepositoryMock.findByAccountId(any())).thenReturn(List.of());

        AccountResponse created = service.createAccount(request);

        assertThat(created.id()).isEqualTo("acc-026");
        assertThat(created.accountHolderName()).isEqualTo("New Holder");
        assertThat(created.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(created.balance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ---- updateAccount ------------------------------------------------------

    @Test
    void updateAccount_appliesOnlyProvidedFields() {
        Account existing = account("acc-001", "Old Name", AccountStatus.ACTIVE);
        when(accountRepositoryMock.findById("acc-001")).thenReturn(Optional.of(existing));
        when(accountRepositoryMock.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepositoryMock.findByAccountId(any())).thenReturn(List.of());

        AccountResponse result = service.updateAccount("acc-001", new UpdateAccountRequest("New Name", null));

        assertThat(result.accountHolderName()).isEqualTo("New Name");
        assertThat(result.status()).isEqualTo(AccountStatus.ACTIVE); // unchanged
    }

    @Test
    void updateAccount_canCloseAnAccount() {
        Account existing = account("acc-001", "Alice", AccountStatus.ACTIVE);
        when(accountRepositoryMock.findById("acc-001")).thenReturn(Optional.of(existing));
        when(accountRepositoryMock.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepositoryMock.findByAccountId(any())).thenReturn(List.of());

        AccountResponse result = service.updateAccount("acc-001", new UpdateAccountRequest(null, AccountStatus.CLOSED));

        assertThat(result.status()).isEqualTo(AccountStatus.CLOSED);
        assertThat(result.accountHolderName()).isEqualTo("Alice"); // unchanged
    }

    @Test
    void updateAccount_whenNotFound_throwsAccountNotFoundException() {
        when(accountRepositoryMock.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateAccount("missing", new UpdateAccountRequest("X", null)))
                .isInstanceOf(AccountNotFoundException.class);
    }

    // ---- deleteAccount (delete guard) --------------------------------------

    @Test
    void deleteAccount_whenNoTransactions_succeeds() {
        Account acc = account("acc-001", "Alice", AccountStatus.ACTIVE);
        when(accountRepositoryMock.findById("acc-001")).thenReturn(Optional.of(acc));
        when(transactionRepositoryMock.findByAccountId("acc-001")).thenReturn(List.of());

        service.deleteAccount("acc-001");

        verify(accountRepositoryMock).deleteById("acc-001");
    }

    @Test
    void deleteAccount_whenHasTransactions_throwsAndDoesNotDelete() {
        Account acc = account("acc-001", "Alice", AccountStatus.ACTIVE);
        when(accountRepositoryMock.findById("acc-001")).thenReturn(Optional.of(acc));
        when(transactionRepositoryMock.findByAccountId("acc-001")).thenReturn(List.of(
                transaction("acc-001", TransactionStatus.POSTED, "10.00")
        ));

        assertThatThrownBy(() -> service.deleteAccount("acc-001"))
                .isInstanceOf(AccountHasTransactionsException.class)
                .hasMessageContaining("acc-001");

        verify(accountRepositoryMock, never()).deleteById(any());
    }

    @Test
    void deleteAccount_whenNotFound_throwsAccountNotFoundException() {
        when(accountRepositoryMock.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteAccount("missing"))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepositoryMock, never()).deleteById(any());
    }
}
