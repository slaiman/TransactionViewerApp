package com.neo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

/**
 * Represents a bank/card account. Deliberately does NOT hold a balance
 * field: balance is a derived value (sum of this account's POSTED
 * transactions), computed on demand from the transaction store rather than
 * stored here — storing it separately would risk it drifting out of sync
 * with the actual transaction history.
 */
@Getter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private String id;

    @Setter
    private String accountHolderName;

    @Setter
    private AccountStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;
}
