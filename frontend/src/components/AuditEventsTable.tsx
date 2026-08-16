import type { AuditEvent } from "../types/audit";

interface AuditEventsTableProps {
  events: AuditEvent[];
}

const timestampFormatter = new Intl.DateTimeFormat("en-US", {
  month: "short",
  day: "2-digit",
  year: "numeric",
  hour: "2-digit",
  minute: "2-digit",
  second: "2-digit",
});

export function AuditEventsTable({ events }: AuditEventsTableProps) {
  if (events.length === 0) {
    return (
      <div className="rounded-md border border-dashed border-ink/15 py-16 text-center">
        <p className="text-sm text-ink/50">No audit events recorded yet.</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-md border border-ink/10 bg-white">
      <table className="w-full min-w-[760px] border-collapse">
        <thead>
          <tr className="border-b border-ink/10 text-left text-xs uppercase tracking-wide text-ink/40">
            <th className="py-3 pr-4 font-medium">Timestamp</th>
            <th className="py-3 pr-4 font-medium">Operation</th>
            <th className="py-3 pr-4 font-medium">Entity</th>
            <th className="py-3 pr-4 font-medium">Account</th>
            <th className="py-3 pr-4 font-medium">Status change</th>
            <th className="py-3 font-medium">Details</th>
          </tr>
        </thead>
        <tbody>
          {events.map((event) => (
            <tr key={event.id} className="border-b border-ink/5 last:border-0 align-top">
              <td className="whitespace-nowrap py-3 pr-4 font-mono text-xs text-ink/60">
                {timestampFormatter.format(new Date(event.timestamp))}
              </td>
              <td className="py-3 pr-4">
                <span className="font-mono text-xs uppercase tracking-wide text-accent">{event.operation}</span>
              </td>
              <td className="whitespace-nowrap py-3 pr-4 font-mono text-xs text-ink/70">
                {event.entityType} <span className="text-ink/40">·</span> {event.entityId}
              </td>
              <td className="whitespace-nowrap py-3 pr-4 font-mono text-xs text-ink/70">{event.accountId}</td>
              <td className="whitespace-nowrap py-3 pr-4 font-mono text-xs text-ink">
                {event.oldStatus ? (
                  <>
                    {event.oldStatus} <span className="text-ink/40">→</span> {event.newStatus}
                  </>
                ) : (
                  event.newStatus ?? "—"
                )}
              </td>
              <td className="py-3 text-sm text-ink/70">{event.details}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
