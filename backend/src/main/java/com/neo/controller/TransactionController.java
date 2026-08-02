package com.neo.controller;

import com.neo.dto.CreateTransactionRequest;
import com.neo.model.Transaction;
import com.neo.model.TransactionStatus;
import com.neo.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * GET /api/transactions/accounts
     * Retrieves all account IDs.
     */
    @GetMapping("/accounts")
    public ResponseEntity<List<String>> getAccounts() {
        return ResponseEntity.ok(transactionService.getAccounts());
    }

    /**
     * GET /api/transactions?accountId=acc-001&status=POSTED
     * Retrieves all transactions for a single account, optionally filtered by status.
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions(
            @RequestParam String accountId,
            @RequestParam(required = false) TransactionStatus status) {
        return ResponseEntity.ok(transactionService.getTransactions(accountId, status));
    }

    /**
     * POST /api/transactions
     * Creates a new transaction (simulating a purchase). Starts as PENDING.
     */
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        Transaction created = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PATCH /api/transactions/{id}/reverse
     * Reverses a transaction. Only valid if the transaction is currently POSTED.
     */
    @PatchMapping("/{id}/reverse")
    public ResponseEntity<Transaction> reverseTransaction(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.reverseTransaction(id));
    }
}