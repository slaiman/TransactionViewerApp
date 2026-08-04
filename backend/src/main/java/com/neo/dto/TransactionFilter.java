package com.neo.dto;

import com.neo.model.TransactionStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Bundles every optional filter/sort query parameter for GET /api/transactions
 * into a single object, rather than threading 8 individual parameters through
 * the controller -> service call.
 */
@Builder
public record TransactionFilter(
        TransactionStatus status,
        LocalDate dateFrom,
        LocalDate dateTo,
        BigDecimal amountMin,
        BigDecimal amountMax,
        String merchant,
        SortBy sortBy,
        SortDirection sortDirection
) {
    public TransactionFilter {
        sortBy = sortBy != null ? sortBy : SortBy.DATE;
        sortDirection = sortDirection != null ? sortDirection : SortDirection.DESC;
    }
}