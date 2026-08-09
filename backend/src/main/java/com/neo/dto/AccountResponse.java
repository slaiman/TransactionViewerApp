package com.neo.dto;

import com.neo.model.AccountStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountResponse(
        String id,
        String accountHolderName,
        AccountStatus status,
        LocalDate createdDate,
        BigDecimal balance
) {}
