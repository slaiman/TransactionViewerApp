package com.neo.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.model.Account;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * In-memory account store, backed by synchronous write-through persistence
 * to a JSON file.
 *
 * Unlike TransactionRepository/TransactionPersistenceService (which use an
 * async write-behind queue with a scheduled flush, justified by transaction
 * volume — thousands of records, created automatically by purchase
 * simulation), accounts are created/updated directly by a person through
 * the UI at a much lower frequency. A synchronous write-through on every
 * mutation is simpler here and gives a stronger durability guarantee (no
 * window where a "successful" response reflects data not yet on disk) —
 * the right tradeoff for this access pattern, not a downgrade from the
 * transaction store's design.
 */
@Slf4j
@Repository
public class AccountRepository {

    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();

    private static final Pattern ID_PATTERN = Pattern.compile("^acc-(\\d+)$");

    /**
     * Seeded from the highest existing "acc-NNN" numeric suffix once, at
     * load time, then incremented atomically for each new account. This
     * makes id generation itself race-free without needing an extra lock:
     * two concurrent createAccount calls calling incrementAndGet() are
     * guaranteed to get two different numbers, never the same one.
     */
    private final AtomicLong idSequence = new AtomicLong(0);

    private final ObjectMapper objectMapper;
    private final Resource internalFile;
    private final Path externalFile;

    public AccountRepository(
            ObjectMapper objectMapper,
            @Value("${app.accounts.internalFile}") Resource internalFile,
            @Value("${app.accounts.externalFile}") String externalFile) {

        this.objectMapper = objectMapper;
        this.internalFile = internalFile;

        try {
            this.externalFile = Paths.get(externalFile);
        } catch (IllegalArgumentException e) {
            log.error("Cannot resolve external accounts file, path is invalid: {}", externalFile);
            throw new IllegalStateException("Invalid path for the external accounts file: " + externalFile, e);
        }
    }

    @PostConstruct
    public synchronized void load() {
        log.info("Loading accounts from file");
        try {
            List<Account> loaded;

            if (!Files.exists(externalFile)) {
                log.info("External accounts file missing, seeding from template");

                Files.createDirectories(externalFile.getParent());
                Files.createFile(externalFile);

                loaded = objectMapper.readValue(internalFile.getInputStream(), new TypeReference<>() {});
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(externalFile.toFile(), loaded);
            } else {
                loaded = objectMapper.readValue(externalFile.toFile(), new TypeReference<>() {});
            }

            for (Account account : loaded) {
                accounts.put(account.getId(), account);
            }

            long maxExistingSuffix = accounts.keySet().stream()
                    .map(ID_PATTERN::matcher)
                    .filter(Matcher::matches)
                    .mapToLong(m -> Long.parseLong(m.group(1)))
                    .max()
                    .orElse(0);
            idSequence.set(maxExistingSuffix);

            log.info("{} accounts loaded, next generated id will be acc-{}", accounts.size(),
                    String.format("%03d", maxExistingSuffix + 1));
        } catch (IOException e) {
            log.error("Cannot load accounts from file", e);
            throw new IllegalStateException("Failed initializing account storage", e);
        }
    }

    public List<Account> findAll() {
        return accounts.values().stream().toList();
    }

    public Optional<Account> findById(String id) {
        return Optional.ofNullable(accounts.get(id));
    }

    /**
     * Generates the next sequential account id, e.g. "acc-026" following
     * "acc-025". Ids matching this pattern in the seed/loaded data are never
     * reused, even if that account is later deleted — the counter only
     * moves forward.
     */
    public String nextId() {
        return String.format("acc-%03d", idSequence.incrementAndGet());
    }

    public synchronized Account save(Account account) {
        accounts.put(account.getId(), account);
        writeThrough();
        return account;
    }

    public synchronized void deleteById(String id) {
        accounts.remove(id);
        writeThrough();
    }

    /**
     * Writes the full in-memory account set to disk atomically (via a temp
     * file + move), synchronously, as part of the calling save/delete —
     * unlike the transaction store, there's no separate scheduled flush.
     */
    private void writeThrough() {
        Path temp = null;
        try {
            temp = Files.createTempFile(externalFile.getParent(), "accounts", ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(temp.toFile(), findAll());
            Files.move(temp, externalFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            deleteQuietly(temp);
            throw new IllegalStateException("Failed to persist accounts to file", e);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to clean up temporary file {}", path, e);
        }
    }
}
