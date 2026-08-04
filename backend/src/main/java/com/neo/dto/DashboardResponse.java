package com.neo.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record DashboardResponse (

    long totalTransactions,

    long postedTransactions,

    long pendingTransactions,

    long reversedTransactions,

    long totalAccounts,

    BigDecimal totalAmount
)
{}
