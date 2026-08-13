import { useCallback, useEffect, useState } from "react";
import {
  fetchAccounts,
  createAccount as apiCreateAccount,
  updateAccount as apiUpdateAccount,
  deleteAccount as apiDeleteAccount,
} from "../api/accountsApi";
import type { Account, CreateAccountRequest, UpdateAccountRequest } from "../types/account";

export function useAccountManagement() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await fetchAccounts();
      setAccounts(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load accounts");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const create = useCallback(
    async (request: CreateAccountRequest) => {
      setError(null);
      try {
        await apiCreateAccount(request);
        await load();
        return true;
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to create account");
        return false;
      }
    },
    [load],
  );

  const update = useCallback(
    async (id: string, request: UpdateAccountRequest) => {
      setError(null);
      try {
        await apiUpdateAccount(id, request);
        await load();
        return true;
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to update account");
        return false;
      }
    },
    [load],
  );

  const remove = useCallback(
    async (id: string) => {
      setError(null);
      try {
        await apiDeleteAccount(id);
        await load();
        return true;
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to delete account");
        return false;
      }
    },
    [load],
  );

  return { accounts, isLoading, error, create, update, remove, refresh: load };
}
