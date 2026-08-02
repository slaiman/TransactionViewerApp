import { useCallback, useEffect, useState } from "react";
import {
  createTransaction as apiCreateTransaction,
  fetchTransactions,
  reverseTransaction as apiReverseTransaction,
} from "../api/transactionsApi";
import type {
  CreateTransactionRequest,
  Transaction,
  TransactionStatus,
} from "../types/transaction";

export function useTransactions(accountId: string, statusFilter?: TransactionStatus) {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!accountId) {
      setTransactions([]);
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const data = await fetchTransactions(accountId, statusFilter);
      setTransactions(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load transactions");
    } finally {
      setIsLoading(false);
    }
  }, [accountId, statusFilter]);

  useEffect(() => {
    load();
  }, [load]);

  const reverse = useCallback(
    async (id: string) => {
      setError(null);
      try {
        await apiReverseTransaction(id);
        await load();
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to reverse transaction");
      }
    },
    [load],
  );

  const create = useCallback(
    async (request: CreateTransactionRequest) => {
      setError(null);
      try {
        await apiCreateTransaction(request);
        await load();
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to create transaction");
      }
    },
    [load],
  );

  return { transactions, isLoading, error, reverse, create, refresh: load };
}
