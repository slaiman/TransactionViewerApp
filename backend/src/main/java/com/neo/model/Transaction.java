package com.neo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@ToString
@EqualsAndHashCode
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

    @Setter
    private TransactionStatus status;
}
