package com.neo.controller;

import com.neo.dto.AccountBalanceResponse;
import com.neo.dto.AccountResponse;
import com.neo.dto.CreateAccountRequest;
import com.neo.dto.UpdateAccountRequest;
import com.neo.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * GET /api/accounts
     * Lists all accounts, each with its current computed balance.
     */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAccounts() {
        return ResponseEntity.ok(accountService.getAccounts());
    }

    /**
     * GET /api/accounts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    /**
     * GET /api/accounts/{id}/balance
     * Dedicated balance-check endpoint (balance is also included on the
     * full AccountResponse, but this is a lighter-weight call when only
     * the balance is needed).
     */
    @GetMapping("/{id}/balance")
    public ResponseEntity<AccountBalanceResponse> getBalance(@PathVariable String id) {
        return ResponseEntity.ok(accountService.getBalance(id));
    }

    /**
     * POST /api/accounts
     * Creates a new account. Starts ACTIVE with a zero balance (no
     * transactions yet).
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse created = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PATCH /api/accounts/{id}
     * Partial update: accountHolderName and/or status. Balance can't be set
     * directly — it's derived from transaction history.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable String id, @RequestBody UpdateAccountRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(id, request));
    }

    /**
     * DELETE /api/accounts/{id}
     * Blocked (409) if the account still has any transaction history.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
