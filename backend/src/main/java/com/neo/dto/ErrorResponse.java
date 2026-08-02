package com.neo.dto;

import lombok.Builder;


import java.time.Instant;

@Builder
public record ErrorResponse(Instant timestamp,
        int status,
        String error,
        String message,
        String path) {

}
