package com.neo.service;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private static final org.slf4j.Logger auditLogger = org.slf4j.LoggerFactory.getLogger("TRANSACTION_LOGGER");

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<AccountResponse> getAccounts() {
        return accountRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public AccountResponse getAccount(String id) {
        Account account = findAccountOrThrow(id);
        return toResponse(account);
    }

    public AccountBalanceResponse getBalance(String id) {
        Account account = findAccountOrThrow(id);
        return new AccountBalanceResponse(account.getId(), computeBalance(account.getId()));
    }

    public AccountResponse createAccount(CreateAccountRequest request) {
        Account account = Account.builder()
                .id(accountRepository.nextId())
                .accountHolderName(request.accountHolderName())
                .status(AccountStatus.ACTIVE)
                .createdDate(LocalDate.now())
                .build();

        Account saved = accountRepository.save(account);

        auditLogger.info(
                "CREATE_ACCOUNT id={} accountHolderName={}",
                saved.getId(),
                saved.getAccountHolderName()
        );

        return toResponse(saved);
    }

    public AccountResponse updateAccount(String id, UpdateAccountRequest request) {
        Account account = findAccountOrThrow(id);

        if (request.accountHolderName() != null && !request.accountHolderName().isBlank()) {
            account.setAccountHolderName(request.accountHolderName());
        }
        if (request.status() != null) {
            account.setStatus(request.status());
        }

        Account saved = accountRepository.save(account);

        auditLogger.info(
                "UPDATE_ACCOUNT id={} accountHolderName={} status={}",
                saved.getId(),
                saved.getAccountHolderName(),
                saved.getStatus()
        );

        return toResponse(saved);
    }

    /**
     * Deletes an account, but only if it has no transaction history — see
     * AccountHasTransactionsException for why deletion is blocked rather
     * than cascaded.
     */
    public void deleteAccount(String id) {
        findAccountOrThrow(id);

        long transactionCount = transactionRepository.findByAccountId(id).size();
        if (transactionCount > 0) {
            throw new AccountHasTransactionsException(id, transactionCount);
        }

        accountRepository.deleteById(id);

        auditLogger.info("DELETE_ACCOUNT id={}", id);
    }

    /**
     * Balance = sum of this account's POSTED transaction amounts. PENDING
     * transactions haven't settled yet, so they don't count; REVERSED
     * transactions were already backed out, so they don't count either.
     */
    private BigDecimal computeBalance(String accountId) {
        return transactionRepository.findByAccountId(accountId).stream()
                .filter(t -> t.getStatus() == TransactionStatus.POSTED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Account findAccountOrThrow(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountHolderName(),
                account.getStatus(),
                account.getCreatedDate(),
                computeBalance(account.getId())
        );
    }
}
