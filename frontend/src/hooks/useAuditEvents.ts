import { useCallback, useEffect, useState } from "react";
import { fetchAuditEvents } from "../api/auditApi";
import type { AuditEvent } from "../types/audit";

export function useAuditEvents() {
  const [events, setEvents] = useState<AuditEvent[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await fetchAuditEvents();
      setEvents(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load audit events");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return { events, isLoading, error, refresh: load };
}
