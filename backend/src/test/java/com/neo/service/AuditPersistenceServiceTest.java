package com.neo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.neo.model.AuditEvent;
import com.neo.repository.AuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuditPersistenceServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private AuditEvent event(String id) {
        return AuditEvent.builder()
                .id(id)
                .operation("CREATE_TRANSACTION")
                .entityType("TRANSACTION")
                .entityId("txn-1")
                .accountId("acc-001")
                .newStatus("PENDING")
                .timestamp(Instant.parse("2026-07-18T10:00:00Z"))
                .details("Transaction created")
                .build();
    }

    @Test
    void load_whenFileDoesNotExist_createsEmptyFileAndLeavesRepositoryEmpty(@TempDir Path tempDir) {
        AuditRepository repository = new AuditRepository();
        String path = tempDir.resolve("audit-events.jsonl").toString();

        AuditPersistenceService service = new AuditPersistenceService(objectMapper, repository, path);
        service.load();

        assertThat(Files.exists(tempDir.resolve("audit-events.jsonl"))).isTrue();
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void load_parsesExistingJsonlLinesIntoRepository(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("audit-events.jsonl");
        Files.writeString(file,
                objectMapper.writeValueAsString(event("evt-1")) + "\n"
                        + objectMapper.writeValueAsString(event("evt-2")) + "\n");

        AuditRepository repository = new AuditRepository();
        AuditPersistenceService service = new AuditPersistenceService(objectMapper, repository, file.toString());
        service.load();

        assertThat(repository.findAll()).extracting(AuditEvent::getId).containsExactlyInAnyOrder("evt-1", "evt-2");
    }

    @Test
    void flush_appendsNewEventsWithoutRewritingExistingLines(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("audit-events.jsonl");
        Files.writeString(file, objectMapper.writeValueAsString(event("evt-existing")) + "\n");

        AuditRepository repository = new AuditRepository();
        AuditPersistenceService service = new AuditPersistenceService(objectMapper, repository, file.toString());
        service.load(); // picks up evt-existing

        service.enqueue(event("evt-new-1"));
        service.enqueue(event("evt-new-2"));
        service.flush();

        List<String> lines = Files.readAllLines(file);
        assertThat(lines).hasSize(3);
        assertThat(lines.get(0)).contains("evt-existing");
        assertThat(lines.get(1)).contains("evt-new-1");
        assertThat(lines.get(2)).contains("evt-new-2");
    }

    @Test
    void flush_withEmptyQueue_doesNotModifyTheFile(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("audit-events.jsonl");
        Files.writeString(file, objectMapper.writeValueAsString(event("evt-1")) + "\n");

        AuditRepository repository = new AuditRepository();
        AuditPersistenceService service = new AuditPersistenceService(objectMapper, repository, file.toString());
        service.load();

        service.flush(); // nothing queued

        assertThat(Files.readAllLines(file)).hasSize(1);
    }
}
