import { useEffect, useState } from "react";
import { fetchAccountIds } from "../api/transactionsApi";

export function useAccounts() {
  const [accountIds, setAccountIds] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setIsLoading(true);
      setError(null);
      try {
        const ids = await fetchAccountIds();
        if (!cancelled) setAccountIds(ids);
      } catch (err) {
        if (!cancelled) setError(err instanceof Error ? err.message : "Failed to load accounts");
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  return { accountIds, isLoading, error };
}
