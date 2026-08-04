import type {
  CreateTransactionRequest,
  Transaction,
  TransactionStatus,
} from "../types/transaction";
import { handleResponse } from "./httpClient";

const BASE_URL = "/api/transactions";

export async function fetchAccountIds(): Promise<string[]> {
  const response = await fetch(`${BASE_URL}/accounts`);
  return handleResponse<string[]>(response);
}

export async function fetchTransactions(
  accountId: string,
  status?: TransactionStatus,
): Promise<Transaction[]> {
  const params = new URLSearchParams({ accountId });
  if (status) params.set("status", status);

  const response = await fetch(`${BASE_URL}?${params.toString()}`);
  return handleResponse<Transaction[]>(response);
}

export async function createTransaction(
  request: CreateTransactionRequest,
): Promise<Transaction> {
  const response = await fetch(BASE_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  });
  return handleResponse<Transaction>(response);
}

export async function reverseTransaction(id: string): Promise<Transaction> {
  const response = await fetch(`${BASE_URL}/${id}/reverse`, {
    method: "PATCH",
  });
  return handleResponse<Transaction>(response);
}
