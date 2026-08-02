import type {
  ApiError,
  CreateTransactionRequest,
  Transaction,
  TransactionStatus,
} from "../types/transaction";

const BASE_URL = "/api/transactions";

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;
    try {
      const body: ApiError = await response.json();
      message = body.message ?? message;
    } catch {
      // response body wasn't JSON (or was empty) — fall back to the generic message
    }
    throw new Error(message);
  }
  // 204 No Content etc. would have no body to parse
  const text = await response.text();
  return text ? (JSON.parse(text) as T) : (undefined as T);
}

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
