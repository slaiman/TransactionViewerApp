package com.neo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    private String id;

    private String operation;

    private String entityType;

    private String entityId;

    private String accountId;

    private String oldStatus;

    private String newStatus;

    private Instant timestamp;

    private String details;
}