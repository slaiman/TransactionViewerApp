package com.neo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.model.Transaction;
import com.neo.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class TransactionPersistenceService {

    // Queue of pending persistence operations.
    // Currently it is used only to trigger snapshot persistence,
    // but it allows future incremental persistence implementations.
    private final BlockingQueue<String> writeQueue = new LinkedBlockingQueue<>();

    //to read and write to Json file
    private final ObjectMapper objectMapper;

    //to interact with the memory repository
    private final TransactionRepository repository;

    //represents the Json data file inside the project
    private final Resource internalFile;

    //represents the Json data file outside the project, used for persistence
    private final Path externalFile;

    public TransactionPersistenceService(
            ObjectMapper objectMapper,
            TransactionRepository repository,
            @Value("${app.data.internalFile}") Resource internalFile,
            @Value("${app.data.externalFile}") String externalFile) {

        this.objectMapper = objectMapper;
        this.repository = repository;
        this.internalFile = internalFile;
        log.info(
                "Initializing transaction persistence service"
        );
        try {
            this.externalFile = Paths.get(externalFile);
        } catch (IllegalArgumentException e) {
            log.error(
                    "Cannot find the external file, Path URI is invalid"
            );
            throw new IllegalStateException("illegal URI format for the file specified", e);
        }
        /*catch (FileSystemNotFoundException e) {
            throw new FileSystemNotFoundException("Cannot resolve external data file");
        }*/
        catch (SecurityException e) {
            log.error(
                    "Cannot access the external file, invalid authorization to the file"
            );
            throw new SecurityException("unauthorized access to the file specified",e);
        }
    }

    @PostConstruct
    public void load() {
        log.info(
                "Start Loading transactions from files"
        );
        try {
            List<Transaction> transactions = null;
            if (!Files.exists(externalFile)) {

                log.info("External data file missing, creating from template");

                Files.createDirectories(externalFile.getParent());

                Files.createFile(externalFile);

                transactions = objectMapper.readValue(internalFile.getInputStream(), new TypeReference<>() {});

                objectMapper.writerWithDefaultPrettyPrinter().writeValue(externalFile.toFile(), transactions);
            }
            else {
                transactions = objectMapper.readValue(externalFile.toFile(), new TypeReference<>() {});
            }

            repository.load(transactions);
            log.info(
                    "Finished Loading transactions from files"
            );
        } catch (IOException e) {
            log.error(
                    "Cannot load transactions from files"
            );
            throw new IllegalStateException("Failed initializing transaction storage", e);
        }
    }

    public void persist(Transaction transaction) {
        writeQueue.offer(transaction.getId());
        log.debug(
                "Transaction added to persistence queue id={}",
                transaction.getId()
        );
    }

    @Scheduled(fixedDelayString = "${app.persistence.flush-delay}")
    public void flush() {
        log.info(
                "Starting transaction flush at {}", Instant.now()
        );
        if (writeQueue.isEmpty()) {
            return;
        }

        List<String> batch = new ArrayList<>();
        writeQueue.drainTo(batch);

        List<Transaction> snapshot = repository.findAllTransactions();
        try {

            Path temp = Files.createTempFile(externalFile.getParent(), "transactions", ".tmp");

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(temp.toFile(), snapshot);

            Files.move(temp, externalFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

            log.info(
                    "Transaction persistence completed batchSize={}",
                    snapshot.size()
            );
        }
        catch (IOException e) {
            batch.forEach(writeQueue::offer);
            log.error("Failed persisting transactions to external file", e);
        }
    }
}
