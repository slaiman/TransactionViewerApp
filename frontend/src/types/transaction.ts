export type TransactionStatus = "POSTED" | "PENDING" | "REVERSED";

export interface Transaction {
  id: string;
  accountId: string;
  date: string; // ISO date string, e.g. "2026-07-18"
  merchantName: string;
  amount: number;
  status: TransactionStatus;
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
