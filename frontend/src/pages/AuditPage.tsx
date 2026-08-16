import { useAuditEvents } from "../hooks/useAuditEvents";
import { AuditEventsTable } from "../components/AuditEventsTable";

export function AuditPage() {
  const { events, isLoading, error } = useAuditEvents();

  return (
    <div>
      <header className="mb-8">
        <p className="font-mono text-xs uppercase tracking-widest text-ink/40">All activity</p>
        <h1 className="mt-1 text-2xl font-semibold text-ink">Audit Log</h1>
      </header>

      {error && (
        <div className="mb-4 rounded-md border border-status-reversed/30 bg-status-reversed/5 px-4 py-3 text-sm text-ink">
          {error}
        </div>
      )}

      {isLoading ? <p className="text-sm text-ink/40">Loading…</p> : <AuditEventsTable events={events} />}
    </div>
  );
}
