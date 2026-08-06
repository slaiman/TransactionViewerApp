package com.neo.controller;

import com.neo.dto.*;
import com.neo.model.Transaction;
import com.neo.model.TransactionStatus;
import com.neo.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
     * GET /api/transactions?accountId=acc-001&amp;status=POSTED&amp;dateFrom=2026-07-01&amp;dateTo=2026-07-31
     *   &amp;amountMin=10&amp;amountMax=500&amp;merchant=whole+foods&amp;sortBy=AMOUNT&amp;sortDirection=ASC
     * Retrieves all transactions for a single account, narrowed by any combination of the optional
     * filters and sorted by sortBy/sortDirection (defaults to date, descending).
     */
    @GetMapping("/filtered")
    public ResponseEntity<PageResponse<Transaction>> getFilteredTransactions(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) BigDecimal amountMin,
            @RequestParam(required = false) BigDecimal amountMax,
            @RequestParam(required = false) String merchant,
            @RequestParam(required = false) SortBy sortBy,
            @RequestParam(required = false) SortDirection sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        TransactionFilter filter = TransactionFilter.builder()
                .status(status)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .amountMin(amountMin)
                .amountMax(amountMax)
                .merchant(merchant)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        PaginationRequest request = PaginationRequest.builder()
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(transactionService.getTransactions(accountId, filter, request));
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