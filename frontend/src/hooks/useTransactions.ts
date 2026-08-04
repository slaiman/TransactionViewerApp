import { useState, useEffect, useCallback } from 'react';
import type { Transaction, TransactionFilterParams } from '../types/transaction';
import { fetchFilteredTransactions } from '../api/transactionsApi';

export const useTransactions = (accountId: string) => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<TransactionFilterParams>({
    sortBy: 'DATE',
    sortDirection: 'DESC',
  });

  const loadTransactions = useCallback(async () => {
    console.log("loadTransactions triggered with accountId:", accountId);

    if (!accountId) {
      console.warn("loadTransactions skipped: accountId is missing!");
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      console.log("Executing fetch with filter:", { ...filter, accountId });

      const data = await fetchFilteredTransactions({ ...filter, accountId });
      console.log("Fetched data successfully:", data);
      setTransactions(data);
    } catch (err: any) {
      console.error("Error fetching transactions:", err);
      setError(err.message || 'Failed to fetch transactions');
    } finally {
      setLoading(false);
    }
  }, [accountId, filter]);

  useEffect(() => {
    loadTransactions();
  }, [loadTransactions]);

  return { transactions, loading, error, filter, setFilter, refetch: loadTransactions };
};