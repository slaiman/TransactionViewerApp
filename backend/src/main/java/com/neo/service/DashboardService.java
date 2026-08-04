package com.neo.service;

import com.neo.dto.DashboardResponse;
import com.neo.model.Transaction;
import com.neo.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;

    /**
     * Aggregates counts and totals across every transaction in the system,
     * regardless of account. Computed on demand with a single pass over the
     * in-memory transaction store — at the current data scale (a few
     * thousand records) this is well under a millisecond, so there's no
     * need to cache or precompute it.
     */
    public DashboardResponse getDashboardStats() {
        List<Transaction> all = transactionRepository.findAllTransactions();

        long posted = 0;
        long pending = 0;
        long reversed = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Transaction transaction : all) {
            totalAmount = totalAmount.add(transaction.getAmount());
            switch (transaction.getStatus()) {
                case POSTED -> posted++;
                case PENDING -> pending++;
                case REVERSED -> reversed++;
            }
        }

        DashboardResponse stats = DashboardResponse.builder()
                .totalTransactions(all.size())
                .postedTransactions(posted)
                .pendingTransactions(pending)
                .reversedTransactions(reversed)
                .totalAccounts(transactionRepository.findAllAccounts().size())
                .totalAmount(totalAmount).build();

        log.debug("Computed dashboard stats: {}", stats);

        return stats;
    }
}