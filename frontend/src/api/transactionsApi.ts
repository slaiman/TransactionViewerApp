import type {
  CreateTransactionRequest,
  Transaction,
  TransactionStatus,
  TransactionFilterParams,
} from "../types/transaction";
import { handleResponse } from "./httpClient";
import axios from 'axios';

const BASE_URL = "/api/transactions";

export async function fetchAccountIds(): Promise<string[]> {
  const response = await fetch(`${BASE_URL}/accounts`);
  return handleResponse<string[]>(response);
}


export async function fetchFilteredTransactions(
  filter: TransactionFilterParams
): Promise<Transaction[]> {
  const response = await axios.get<Transaction[]>(
      `${BASE_URL}/filtered`,
      { params: filter } // Axios converts this into ?accountId=...&status=...&sortBy=...
      );
  return response.data;
};

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
