import type { TransactionStatus } from "../types/transaction";

const STATUS_CONFIG: Record<TransactionStatus, { label: string; dotClass: string; textClass: string }> = {
  POSTED: { label: "Posted", dotClass: "bg-status-posted", textClass: "text-status-posted" },
  PENDING: { label: "Pending", dotClass: "bg-status-pending", textClass: "text-status-pending" },
  REVERSED: { label: "Reversed", dotClass: "bg-status-reversed", textClass: "text-status-reversed" },
};

export function StatusBadge({ status }: { status: TransactionStatus }) {
  const config = STATUS_CONFIG[status];
  return (
    <span className="inline-flex items-center gap-1.5 font-mono text-xs uppercase tracking-wide">
      <span className={`h-1.5 w-1.5 rounded-full ${config.dotClass}`} aria-hidden="true" />
      <span className={config.textClass}>{config.label}</span>
    </span>
  );
}
