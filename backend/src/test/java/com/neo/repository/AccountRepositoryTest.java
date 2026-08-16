package com.neo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRepositoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private AccountRepository repositoryWithSeed(String seedJson, Path tempDir) {
        ByteArrayResource internal = new ByteArrayResource(seedJson.getBytes(StandardCharsets.UTF_8));
        String externalPath = tempDir.resolve("accounts.json").toString();
        AccountRepository repository = new AccountRepository(objectMapper, internal, externalPath);
        repository.load();
        return repository;
    }

    @Test
    void nextId_continuesFromHighestExistingSuffix(@TempDir Path tempDir) {
        String seedJson = """
                [
                  {"id": "acc-001", "accountHolderName": "Alice", "status": "ACTIVE", "createdDate": "2025-01-15"},
                  {"id": "acc-025", "accountHolderName": "Yuki", "status": "ACTIVE", "createdDate": "2025-01-15"}
                ]
                """;

        AccountRepository repository = repositoryWithSeed(seedJson, tempDir);

        assertThat(repository.nextId()).isEqualTo("acc-026");
        assertThat(repository.nextId()).isEqualTo("acc-027");
    }

    @Test
    void nextId_startsAtOneWhenNoExistingAccounts(@TempDir Path tempDir) {
        AccountRepository repository = repositoryWithSeed("[]", tempDir);

        assertThat(repository.nextId()).isEqualTo("acc-001");
    }

    @Test
    void nextId_ignoresIdsThatDontMatchTheAccPattern(@TempDir Path tempDir) {
        String seedJson = """
                [
                  {"id": "legacy-import-1", "accountHolderName": "Legacy", "status": "ACTIVE", "createdDate": "2025-01-15"},
                  {"id": "acc-005", "accountHolderName": "Someone", "status": "ACTIVE", "createdDate": "2025-01-15"}
                ]
                """;

        AccountRepository repository = repositoryWithSeed(seedJson, tempDir);

        assertThat(repository.nextId()).isEqualTo("acc-006");
    }

    @Test
    void nextId_neverReusesAnIdAfterDeletion(@TempDir Path tempDir) {
        String seedJson = """
                [
                  {"id": "acc-001", "accountHolderName": "Alice", "status": "ACTIVE", "createdDate": "2025-01-15"}
                ]
                """;

        AccountRepository repository = repositoryWithSeed(seedJson, tempDir);

        String generated = repository.nextId(); // "acc-002"
        repository.save(com.neo.model.Account.builder()
                .id(generated)
                .accountHolderName("New")
                .status(com.neo.model.AccountStatus.ACTIVE)
                .createdDate(java.time.LocalDate.now())
                .build());
        repository.deleteById(generated);

        // the counter only moves forward — acc-002 is not handed out again
        assertThat(repository.nextId()).isEqualTo("acc-003");
    }
}
