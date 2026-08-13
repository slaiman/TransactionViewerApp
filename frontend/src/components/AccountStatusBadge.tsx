import type { AccountStatus } from "../types/account";

const STATUS_CONFIG: Record<AccountStatus, { label: string; dotClass: string; textClass: string }> = {
  ACTIVE: { label: "Active", dotClass: "bg-status-posted", textClass: "text-status-posted" },
  CLOSED: { label: "Closed", dotClass: "bg-status-reversed", textClass: "text-status-reversed" },
};

export function AccountStatusBadge({ status }: { status: AccountStatus }) {
  const config = STATUS_CONFIG[status];
  return (
    <span className="inline-flex items-center gap-1.5 font-mono text-xs uppercase tracking-wide">
      <span className={`h-1.5 w-1.5 rounded-full ${config.dotClass}`} aria-hidden="true" />
      <span className={config.textClass}>{config.label}</span>
    </span>
  );
}
