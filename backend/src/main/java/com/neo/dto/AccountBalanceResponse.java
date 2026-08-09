package com.neo.dto;

import java.math.BigDecimal;

public record AccountBalanceResponse(
        String accountId,
        BigDecimal balance
) {}
