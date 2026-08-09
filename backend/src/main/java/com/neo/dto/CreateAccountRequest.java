package com.neo.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(
        @NotBlank(message = "accountHolderName is required")
        String accountHolderName
) {}
