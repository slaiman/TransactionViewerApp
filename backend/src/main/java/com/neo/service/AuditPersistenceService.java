package com.neo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.model.AuditEvent;
import com.neo.repository.AuditRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Persists audit events to a newline-delimited JSON (JSONL) file — one JSON
 * object per line.
 *
 * Unlike TransactionPersistenceService (which rewrites a full snapshot of
 * the entire transaction set on every flush, since a transaction's status
 * can change after it's created), audit events are immutable and
 * append-only once written. So each flush only needs to append whatever
 * accumulated in the queue since the last flush, not rewrite the whole
 * history — flush cost is O(new events), not O(total history), which
 * matters here specifically because the audit log only ever grows.
 */
@Slf4j
@Service
public class AuditPersistenceService {

    private final BlockingQueue<AuditEvent> writeQueue = new LinkedBlockingQueue<>();

    private final ObjectMapper objectMapper;
    private final AuditRepository auditRepository;
    private final Path externalFile;

    public AuditPersistenceService(
            ObjectMapper objectMapper,
            AuditRepository auditRepository,
            @Value("${app.audit.externalFile}") String externalFile) {

        this.objectMapper = objectMapper;
        this.auditRepository = auditRepository;

        try {
            this.externalFile = Paths.get(externalFile);
        } catch (IllegalArgumentException e) {
            log.error("Cannot resolve external audit file, path is invalid: {}", externalFile);
            throw new IllegalStateException("Invalid path for the external audit file: " + externalFile, e);
        }
    }

    @PostConstruct
    public void load() {
        log.info("Loading audit events from file");
        try {
            if (!Files.exists(externalFile)) {
                log.info("No existing audit file at {} — starting with an empty audit log", externalFile);
                if (externalFile.getParent() != null) {
                    Files.createDirectories(externalFile.getParent());
                }
                Files.createFile(externalFile);
                return;
            }

            List<AuditEvent> loaded = new ArrayList<>();
            for (String line : Files.readAllLines(externalFile)) {
                if (line.isBlank()) {
                    continue;
                }
                loaded.add(objectMapper.readValue(line, AuditEvent.class));
            }

            auditRepository.loadAll(loaded);
        } catch (IOException e) {
            log.error("Cannot load audit events from file", e);
            throw new IllegalStateException("Failed initializing audit storage", e);
        }
    }

    /**
     * Queues an event to be appended to the file on the next scheduled
     * flush. Does not write to disk directly — callers get an immediate
     * response while the actual write happens asynchronously.
     */
    public void enqueue(AuditEvent event) {
        writeQueue.offer(event);
    }

    @Scheduled(fixedDelayString = "${app.persistence.flush-delay}")
    public void flush() {
        if (writeQueue.isEmpty()) {
            return;
        }

        List<AuditEvent> batch = new ArrayList<>();
        writeQueue.drainTo(batch);

        try {
            List<String> lines = new ArrayList<>(batch.size());
            for (AuditEvent event : batch) {
                lines.add(objectMapper.writeValueAsString(event));
            }
            Files.write(externalFile, lines, StandardOpenOption.APPEND);

            log.info("Appended {} audit event(s) to file", batch.size());
        } catch (IOException e) {
            // put the batch back so it's retried on the next flush, rather than lost
            batch.forEach(writeQueue::offer);
            log.error("Failed to persist audit events to file, will retry on next flush", e);
        }
    }
}
