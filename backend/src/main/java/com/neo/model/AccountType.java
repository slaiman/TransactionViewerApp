package com.neo.model;

import lombok.Getter;

@Getter
public enum AccountType {
    ALL_ACCOUNTS("ALL Accounts");

    // Getter method to retrieve the string
    private final String displayName;

    // Constructor
    AccountType(String displayName) {
        this.displayName = displayName;
    }

}
