package com.neo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private String id;

    private String accountId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String merchantName;

    private BigDecimal amount;

    private TransactionStatus status;
}
