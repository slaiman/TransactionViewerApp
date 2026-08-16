package com.neo.repository;

import com.neo.model.AuditEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class AuditRepository {

    private final ConcurrentHashMap<String, AuditEvent> events =
            new ConcurrentHashMap<>();

    public AuditEvent save(AuditEvent event) {

        log.info(
                "Saving audit event id={} operation={}",
                event.getId(),
                event.getOperation()
        );

        events.put(event.getId(), event);

        return event;
    }

    public List<AuditEvent> findAll() {

        log.info("Retrieving all audit events");

        return events.values()
                .stream()
                .sorted(
                        Comparator.comparing(AuditEvent::getTimestamp)
                                .reversed()
                )
                .toList();
    }
}