export type TransactionStatus = "POSTED" | "PENDING" | "REVERSED";

export type SortBy = 'DATE' | 'AMOUNT';
export type SortDirection = 'ASC' | 'DESC';

// Account type Enum
export enum AccountFilterOption {
  ALL = 'ALL Accounts',
}

export interface TransactionFilterParams {
  accountId?: string;
  status?: TransactionStatus;
  dateFrom?: string;      // YYYY-MM-DD
  dateTo?: string;        // YYYY-MM-DD
  amountMin?: number;
  amountMax?: number;
  merchant?: string;
  sortBy?: SortBy;
  sortDirection?: SortDirection;
}

export interface Transaction {
  id: string;
  accountId: string;
  date: string; // ISO date string, e.g. "2026-07-18"
  merchantName: string;
  amount: number;
  status: TransactionStatus;
}

export interface PaginationParams {
  page: number;      // 0-indexed page number
  size: number;      // items per page
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

export interface CreateTransactionRequest {
  accountId: string;
  date?: string;
  merchantName: string;
  amount: number;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

export interface DashboardStats {
  totalTransactions: number;
  postedTransactions: number;
  pendingTransactions: number;
  reversedTransactions: number;
  totalAccounts: number;
  totalAmount: number;
}
