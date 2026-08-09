package com.neo.exception;

/**
 * Thrown when an account is about to be deleted but still has transaction
 * history. Deleting it anyway would leave those transactions pointing at a
 * now-nonexistent accountId, so deletion is blocked rather than cascaded.
 */
public class AccountHasTransactionsException extends RuntimeException {
    public AccountHasTransactionsException(String accountId, long transactionCount) {
        super("Cannot delete account " + accountId + ": it still has " + transactionCount
                + " transaction(s). Close the account instead, or remove its transactions first.");
    }
}
