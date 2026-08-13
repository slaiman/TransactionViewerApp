import type { Account, AccountBalance, CreateAccountRequest, UpdateAccountRequest } from "../types/account";
import { handleResponse } from "./httpClient";

const BASE_URL = "/api/accounts";

export async function fetchAccounts(): Promise<Account[]> {
  const response = await fetch(BASE_URL);
  return handleResponse<Account[]>(response);
}

export async function fetchAccountBalance(id: string): Promise<AccountBalance> {
  const response = await fetch(`${BASE_URL}/${id}/balance`);
  return handleResponse<AccountBalance>(response);
}

export async function createAccount(request: CreateAccountRequest): Promise<Account> {
  const response = await fetch(BASE_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  });
  return handleResponse<Account>(response);
}

export async function updateAccount(id: string, request: UpdateAccountRequest): Promise<Account> {
  const response = await fetch(`${BASE_URL}/${id}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  });
  return handleResponse<Account>(response);
}

export async function deleteAccount(id: string): Promise<void> {
  const response = await fetch(`${BASE_URL}/${id}`, {
    method: "DELETE",
  });
  return handleResponse<void>(response);
}
