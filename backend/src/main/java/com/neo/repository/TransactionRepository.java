package com.neo.repository;

import com.neo.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Repository
public class TransactionRepository {

    // In-memory database for transactions (transaction ID, Transaction Object)
    private final ConcurrentHashMap<String, Transaction> transactions = new ConcurrentHashMap<>();

    // In-memory database for accounts and their transactions for better search (Account ID, Set of transaction IDs)
    private final ConcurrentHashMap<String, Set<String>> accountTransactions = new ConcurrentHashMap<>();

    public void load(List<Transaction> loadedTransactions) {

        log.info(
                "Executing 'load' method in TransactionRepository"
        );

        //loop over all transactions
        for (Transaction transaction : loadedTransactions) {

            //insert transactions into memory
            transactions.put(
                    transaction.getId(),
                    transaction
            );

            //insert account ID and its transaction IDs into memory
            accountTransactions
                    .computeIfAbsent(
                            transaction.getAccountId(),
                            key -> ConcurrentHashMap.newKeySet()
                    )
                    .add(transaction.getId());
        }

        log.info("{} transactions loaded", loadedTransactions.size());
    }

    public List<String> findAllAccounts(){
        log.info(
                "Executing 'findAllAccounts' method in TransactionRepository"
        );
        return accountTransactions.keySet().stream().toList();
    }

    public List<Transaction> findAllTransactions(){
        log.info(
                "Executing 'findAllTransactions' method in TransactionRepository"
        );
        return transactions.values().stream().toList();
    }

    public Optional<Transaction> findById(String id) {
        log.info(
                "Executing 'findById' method in TransactionRepository"
        );
        return Optional.ofNullable(transactions.get(id));
    }

    public List<Transaction> findByAccountId(String accountId) {

        log.info(
                "Executing 'findByAccountId' method in TransactionRepository"
        );

        //get the transaction IDs related to the account ID
        Set<String> trans = accountTransactions.get(accountId);
        if (trans == null || trans.isEmpty()) {
            return List.of();
        }
        //return list of transactions based on their IDs
        return trans.stream()
                .map(transactions::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public Transaction save(Transaction transaction) {

        log.info(
                "Executing 'save' method in TransactionRepository"
        );

        // Update memory immediately
        transactions.put(transaction.getId(), transaction);

        // Update account transaction hashmap by the account id and the transaction ids
        accountTransactions
                .computeIfAbsent(
                        transaction.getAccountId(),
                        key -> ConcurrentHashMap.newKeySet()
                )
                .add(transaction.getId());
        return transaction;
    }

    public Transaction update(Transaction transaction) {

        log.info(
                "Executing 'update' method in TransactionRepository"
        );

        Transaction old =
                transactions.put(transaction.getId(), transaction);

        // In case accountId changes
        if (old != null && !old.getAccountId().equals(transaction.getAccountId())) {

            Set<String> oldAccountTransactions = accountTransactions.get(old.getAccountId());

            if (oldAccountTransactions != null && !oldAccountTransactions.isEmpty()) {
                oldAccountTransactions.remove(transaction.getId());

                if (oldAccountTransactions.isEmpty()) {
                    accountTransactions.remove(old.getAccountId(), oldAccountTransactions);
                }
            }

            accountTransactions
                    .computeIfAbsent(
                            transaction.getAccountId(),
                            key -> ConcurrentHashMap.newKeySet()
                    )
                    .add(transaction.getId());
        }
        return transaction;
    }
}