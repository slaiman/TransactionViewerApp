import { StatCard } from "../components/StatCard";
import { useDashboard } from "../hooks/useDashboard";

const currencyFormatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

const numberFormatter = new Intl.NumberFormat("en-US");

export function DashboardPage() {
  const { stats, isLoading, error } = useDashboard();

  return (
    <div>
      <header className="mb-8">
        <p className="font-mono text-xs uppercase tracking-widest text-ink/40">All accounts</p>
        <h1 className="mt-1 text-2xl font-semibold text-ink">Dashboard</h1>
      </header>

      {isLoading && !stats && <p className="text-sm text-ink/40">Loading…</p>}

      {error && (
        <div className="mb-4 rounded-md border border-status-reversed/30 bg-status-reversed/5 px-4 py-3 text-sm text-ink">
          {error}
        </div>
      )}

      {stats && (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
          <StatCard label="Total transactions" value={numberFormatter.format(stats.totalTransactions)} />
          <StatCard label="Total accounts" value={numberFormatter.format(stats.totalAccounts)} />
          <StatCard label="Total amount" value={currencyFormatter.format(stats.totalAmount)} />
          <StatCard
            label="Posted"
            value={numberFormatter.format(stats.postedTransactions)}
            accentClass="text-status-posted"
          />
          <StatCard
            label="Pending"
            value={numberFormatter.format(stats.pendingTransactions)}
            accentClass="text-status-pending"
          />
          <StatCard
            label="Reversed"
            value={numberFormatter.format(stats.reversedTransactions)}
            accentClass="text-status-reversed"
          />
        </div>
      )}
    </div>
  );
}
