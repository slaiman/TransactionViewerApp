import { useState, useEffect, useCallback } from 'react';
import type { CreateTransactionRequest, Transaction, TransactionFilterParams, PageResponse } from '../types/transaction';
import {
  fetchFilteredTransactions,
  createTransaction as apiCreateTransaction,
  confirmTransaction as apiConfirmTransaction,
  reverseTransaction as apiReverseTransaction,
} from '../api/transactionsApi';

export const useTransactions = (initialAccountId: string) => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [pageInfo, setPageInfo] = useState<PageResponse<Transaction> | null>(null);
  const [page, setPage] = useState<number>(0);
  const [pageSize, setPageSize] = useState<number>(10);

  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Filter state
  const [filter, setFilter] = useState<TransactionFilterParams>({
    accountId: initialAccountId,
    sortBy: 'DATE',
    sortDirection: 'DESC',
  });

  const loadTransactions = useCallback(async () => {
    // Determine the active account ID either from filter state or initial prop
    const activeAccountId = filter.accountId || initialAccountId;

    console.log("loadTransactions triggered with accountId:", activeAccountId);

    if (!activeAccountId && filter.accountId !== undefined) {
      console.warn("loadTransactions skipped: accountId is missing!");
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const requestFilter = {
        ...filter,
        accountId: activeAccountId,
      };

      console.log("Executing fetch with filter:", requestFilter, "page:", page, "size:", pageSize);

      // fetchFilteredTransactions returns PageResponse<Transaction>
      const response: PageResponse<Transaction> = await fetchFilteredTransactions(requestFilter, {
        page,
        size: pageSize,
      });
      console.log("Fetched data successfully:", response);

      // Set items and full page Metadata
      setTransactions(response.content || []);
      setPageInfo(response);
    } catch (err: any) {
      console.error("Error fetching transactions:", err);
      setError(err.message || 'Failed to fetch transactions');
    } finally {
      setLoading(false);
    }
  }, [filter, page, pageSize, initialAccountId]);

  useEffect(() => {
    loadTransactions();
  }, [loadTransactions]);

  // Wrapper for setFilter that resets to page 0 whenever filter conditions change
  const handleSetFilter = (
    value: React.SetStateAction<TransactionFilterParams>
  ) => {
    setFilter(value);
    setPage(0); // Go back to first page on filter change
  };

  const create = useCallback(
    async (request: CreateTransactionRequest) => {
      setError(null);
      try {
        await apiCreateTransaction(request);
        await loadTransactions();
      } catch (err: any) {
        console.error("Error creating transaction:", err);
        setError(err.message || 'Failed to create transaction');
      }
    },
    [loadTransactions],
  );

  const confirm = useCallback(
    async (id: string) => {
      setError(null);
      try {
        await apiConfirmTransaction(id);
        await loadTransactions();
      } catch (err: any) {
        console.error("Error confirming transaction:", err);
        setError(err.message || 'Failed to confirm transaction');
      }
    },
    [loadTransactions],
  );

  const reverse = useCallback(
    async (id: string) => {
      setError(null);
      try {
        await apiReverseTransaction(id);
        await loadTransactions();
      } catch (err: any) {
        console.error("Error reversing transaction:", err);
        setError(err.message || 'Failed to reverse transaction');
      }
    },
    [loadTransactions],
  );

  return {
    transactions,
    pageInfo,
    page,
    setPage,
    pageSize,
    setPageSize,
    loading,
    error,
    filter,
    setFilter: handleSetFilter,
    create,
    confirm,
    reverse,
    refetch: loadTransactions,
  };
};