package com.neo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(@NotBlank(message = "accountId is required")
                                       String accountId,

                                       @JsonFormat(pattern = "yyyy-MM-dd")
                                       LocalDate date,

                                       @NotBlank(message = "merchantName is required")
                                       String merchantName,

                                        @NotNull(message = "amount is required")
                                        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
                                        BigDecimal amount)
        {}
