import type { Account } from "../types/account";
import { AccountStatusBadge } from "./AccountStatusBadge";

interface AccountsTableProps {
  accounts: Account[];
  onEdit: (account: Account) => void;
  onDelete: (account: Account) => void;
}

const currencyFormatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

const dateFormatter = new Intl.DateTimeFormat("en-US", {
  month: "short",
  day: "2-digit",
  year: "numeric",
});

export function AccountsTable({ accounts, onEdit, onDelete }: AccountsTableProps) {
  if (accounts.length === 0) {
    return (
      <div className="rounded-md border border-dashed border-ink/15 py-16 text-center">
        <p className="text-sm text-ink/50">No accounts yet.</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-md border border-ink/10 bg-white">
      <table className="w-full min-w-[720px] border-collapse">
        <thead>
          <tr className="border-b border-ink/10 text-left text-xs uppercase tracking-wide text-ink/40">
            <th className="py-3 pr-4 font-medium">Account</th>
            <th className="py-3 pr-4 font-medium">Holder</th>
            <th className="py-3 pr-4 font-medium">Opened</th>
            <th className="py-3 pr-4 font-medium">Status</th>
            <th className="py-3 pr-4 text-right font-medium">Balance</th>
            <th className="py-3 font-medium" />
          </tr>
        </thead>
        <tbody>
          {accounts.map((account) => (
            <tr key={account.id} className="border-b border-ink/5 last:border-0">
              <td className="whitespace-nowrap py-3 pr-4 font-mono text-sm text-ink/70">{account.id}</td>
              <td className="py-3 pr-4 text-sm text-ink">{account.accountHolderName}</td>
              <td className="whitespace-nowrap py-3 pr-4 font-mono text-sm text-ink/70">
                {dateFormatter.format(new Date(account.createdDate))}
              </td>
              <td className="py-3 pr-4">
                <AccountStatusBadge status={account.status} />
              </td>
              <td className="py-3 pr-4 text-right font-mono text-sm tabular-nums text-ink">
                {currencyFormatter.format(account.balance)}
              </td>
              <td className="py-3 text-right">
                <div className="flex justify-end gap-2">
                  <button
                    type="button"
                    onClick={() => onEdit(account)}
                    className="rounded border border-ink/15 px-2.5 py-1 text-xs font-medium text-ink/70 transition-colors hover:border-accent hover:text-accent focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
                  >
                    Edit
                  </button>
                  <button
                    type="button"
                    onClick={() => onDelete(account)}
                    className="rounded border border-ink/15 px-2.5 py-1 text-xs font-medium text-ink/70 transition-colors hover:border-status-reversed hover:text-status-reversed focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
                  >
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
