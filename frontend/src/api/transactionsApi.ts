import type {
  CreateTransactionRequest,
  PageResponse,
  PaginationParams,
  Transaction,
  TransactionFilterParams,
} from "../types/transaction";
import { handleResponse } from "./httpClient";

const BASE_URL = "/api/transactions";

export async function fetchAccountIds(): Promise<string[]> {
  const response = await fetch(`${BASE_URL}/accounts`);
  return handleResponse<string[]>(response);
}

export async function fetchFilteredTransactions(
  filter: TransactionFilterParams,
  pagination: PaginationParams,
): Promise<PageResponse<Transaction>> {
  const params = new URLSearchParams();

  // Only send params that actually have a value — an empty string or
  // undefined filter field should be omitted rather than sent as "".
  Object.entries({ ...filter, ...pagination }).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      params.set(key, String(value));
    }
  });

  const response = await fetch(`${BASE_URL}/filtered?${params.toString()}`);
  return handleResponse<PageResponse<Transaction>>(response);
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