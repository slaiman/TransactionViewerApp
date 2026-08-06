import { useState, useEffect, useCallback } from 'react';
import type { Transaction, TransactionFilterParams, PageResponse } from '../types/transaction';
import { fetchFilteredTransactions } from '../api/transactionsApi';

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

      const requestParams = {
        ...filter,
        accountId: activeAccountId,
        page,
        size: pageSize,
      };

      console.log("Executing fetch with params:", requestParams);

      // fetchFilteredTransactions returns PageResponse<Transaction>
      const response: PageResponse<Transaction> = await fetchFilteredTransactions(requestParams);
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
    refetch: loadTransactions,
  };
};