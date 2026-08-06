import React, { useState, useEffect } from 'react';
import { useTransactions } from '../hooks/useTransactions';
import { useAccounts } from '../hooks/useAccounts';
import type { SortBy, SortDirection, TransactionStatus } from '../types/transaction';
import { TransactionList } from '../components/TransactionList';
import { AccountFilterOption } from '../types/transaction';
import { Pagination } from '../components/Pagination';

export const TransactionsPage: React.FC = () => {
  // 1. Fetch account IDs
  const { accountIds, isLoading: isLoadingAccounts, error: accountsError } = useAccounts();
  const [selectedAccountId, setSelectedAccountId] = useState<string>('');

  // 2. Default to the first account ID once loaded
  useEffect(() => {
    if (accountIds && accountIds.length > 0 && !selectedAccountId) {
      setSelectedAccountId(accountIds[0]);
    }
  }, [accountIds, selectedAccountId]);

  // 3. Fetch transactions using the hook
  const {
    transactions,
    pageInfo,
    page,
    setPage,
    loading,
    error,
    filter,
    setFilter
  } = useTransactions(selectedAccountId);

  // Sync selectedAccountId state changes with filter state
  const handleAccountChange = (accId: string) => {
    setSelectedAccountId(accId);
    handleFilterChange('accountId', accId === AccountFilterOption.ALL ? undefined : accId);
  };

  const handleFilterChange = (field: keyof typeof filter, value: any) => {
    setFilter((prev) => ({
      ...prev,
      [field]: value === '' ? undefined : value,
    }));
  };

  const handleResetFilters = () => {
    setFilter({
      accountId: selectedAccountId === AccountFilterOption.ALL ? undefined : selectedAccountId,
      sortBy: 'DATE',
      sortDirection: 'DESC',
    });
  };

  // Guard States
  if (isLoadingAccounts) {
    return <div className="p-6 text-gray-500">Loading accounts...</div>;
  }

  if (accountsError) {
    return <div className="p-6 text-red-500">{accountsError}</div>;
  }

  if (!accountIds || accountIds.length === 0) {
    return <div className="p-6 text-gray-500">No accounts found.</div>;
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-800">Transaction History</h1>

        {/* Account Selector Dropdown */}
        <div className="flex items-center space-x-2">
          <label className="text-sm font-medium text-gray-700">Account:</label>
          <select
            value={selectedAccountId}
            onChange={(e) => handleAccountChange(e.target.value)}
            className="border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value={AccountFilterOption.ALL}>All Accounts</option>
            {accountIds.map((accId: string) => (
              <option key={accId} value={accId}>
                Account #{accId}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Filter Control Panel */}
      <div className="bg-white p-4 rounded-lg border border-gray-200 shadow-sm space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">

          {/* Merchant Search */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1">
              Merchant
            </label>
            <input
              type="text"
              className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              placeholder="Search merchant..."
              value={filter.merchant || ''}
              onChange={(e) => handleFilterChange('merchant', e.target.value)}
            />
          </div>

          {/* Status Filter */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1">
              Status
            </label>
            <select
              className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              value={filter.status || ''}
              onChange={(e) => handleFilterChange('status', e.target.value as TransactionStatus)}
            >
              <option value="">All Statuses</option>
              <option value="POSTED">Posted</option>
              <option value="PENDING">Pending</option>
              <option value="REVERSED">Reversed</option>
            </select>
          </div>

          {/* Date From */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1">
              From Date
            </label>
            <input
              type="date"
              className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              value={filter.dateFrom || ''}
              onChange={(e) => handleFilterChange('dateFrom', e.target.value)}
            />
          </div>

          {/* Date To */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1">
              To Date
            </label>
            <input
              type="date"
              className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              value={filter.dateTo || ''}
              onChange={(e) => handleFilterChange('dateTo', e.target.value)}
            />
          </div>

          {/* Min Amount */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1">
              Min Amount ($)
            </label>
            <input
              type="number"
              step="0.01"
              className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              placeholder="0.00"
              value={filter.amountMin ?? ''}
              onChange={(e) =>
                handleFilterChange('amountMin', e.target.value ? Number(e.target.value) : undefined)
              }
            />
          </div>

          {/* Max Amount */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1">
              Max Amount ($)
            </label>
            <input
              type="number"
              step="0.01"
              className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              placeholder="1000.00"
              value={filter.amountMax ?? ''}
              onChange={(e) =>
                handleFilterChange('amountMax', e.target.value ? Number(e.target.value) : undefined)
              }
            />
          </div>

          {/* Sort By */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1">
              Sort By
            </label>
            <select
              className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              value={filter.sortBy || 'DATE'}
              onChange={(e) => handleFilterChange('sortBy', e.target.value as SortBy)}
            >
              <option value="DATE">Date</option>
              <option value="AMOUNT">Amount</option>
            </select>
          </div>

          {/* Sort Direction */}
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1">
              Direction
            </label>
            <select
              className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              value={filter.sortDirection || 'DESC'}
              onChange={(e) => handleFilterChange('sortDirection', e.target.value as SortDirection)}
            >
              <option value="DESC">Descending</option>
              <option value="ASC">Ascending</option>
            </select>
          </div>

        </div>

        <div className="flex justify-end">
          <button
            onClick={handleResetFilters}
            className="px-3 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-600 rounded-md text-sm font-medium transition"
          >
            Reset Filters
          </button>
        </div>
      </div>

      {/* Render Table and Pagination Controls */}
      {loading ? (
        <div className="py-8 text-center text-gray-500">Loading transactions...</div>
      ) : error ? (
        <div className="p-4 bg-red-50 text-red-600 rounded-md border border-red-200">{error}</div>
      ) : (
        <div className="bg-white rounded-lg border border-gray-200 overflow-hidden shadow-sm">
          <TransactionList transactions={transactions} />

          {/* Render Pagination Footer */}
          {pageInfo && pageInfo.totalPages > 0 && (
            <Pagination
              pageNumber={page}
              totalPages={pageInfo.totalPages}
              totalElements={pageInfo.totalElements}
              pageSize={pageInfo.pageSize}
              onPageChange={(newPage) => setPage(newPage)}
            />
          )}
        </div>
      )}
    </div>
  );
};

export default TransactionsPage;