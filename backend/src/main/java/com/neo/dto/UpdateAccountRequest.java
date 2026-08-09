package com.neo.dto;

import com.neo.model.AccountStatus;

/**
 * Partial update payload: both fields are optional, and only non-null
 * fields are applied. Balance is intentionally not present here — it's
 * derived, not directly settable.
 */
public record UpdateAccountRequest(
        String accountHolderName,
        AccountStatus status
) {}
