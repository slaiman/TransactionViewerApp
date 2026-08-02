import { useEffect, useState } from "react";
import { StatusFilter } from "./components/StatusFilter";
import { AccountSwitcher } from "./components/AccountSwitcher";
import { TransactionList } from "./components/TransactionList";
import { NewTransactionForm } from "./components/NewTransactionForm";
import { useAccounts } from "./hooks/useAccounts";
import { useTransactions } from "./hooks/useTransactions";
import type { TransactionStatus } from "./types/transaction";

function App() {
  const { accountIds, error: accountsError } = useAccounts();
  const [selectedAccountId, setSelectedAccountId] = useState<string>("");
  const [statusFilter, setStatusFilter] = useState<TransactionStatus | "ALL">("ALL");

  // Default to the first known account once the account list has loaded.
  useEffect(() => {
    if (!selectedAccountId && accountIds.length > 0) {
      setSelectedAccountId(accountIds[0]);
    }
  }, [accountIds, selectedAccountId]);

  const { transactions, isLoading, error, reverse, create } = useTransactions(
    selectedAccountId,
    statusFilter === "ALL" ? undefined : statusFilter,
  );

  return (
    <div className="min-h-screen bg-paper">
      <div className="mx-auto max-w-4xl px-6 py-10">
        <header className="mb-8 flex items-start justify-between">
          <div>
            <p className="font-mono text-xs uppercase tracking-widest text-ink/40">
              {selectedAccountId || "Loading account…"}
            </p>
            <h1 className="mt-1 text-2xl font-semibold text-ink">Transactions</h1>
          </div>
          <AccountSwitcher
            accountIds={accountIds}
            selectedAccountId={selectedAccountId}
            onChange={setSelectedAccountId}
          />
        </header>

        {selectedAccountId && (
          <div className="mb-6">
            <NewTransactionForm accountId={selectedAccountId} onCreate={create} />
          </div>
        )}

        <div className="mb-4 flex items-center justify-between">
          <StatusFilter value={statusFilter} onChange={setStatusFilter} />
          {isLoading && <span className="text-xs text-ink/40">Loading…</span>}
        </div>

        {(error || accountsError) && (
          <div className="mb-4 rounded-md border border-status-reversed/30 bg-status-reversed/5 px-4 py-3 text-sm text-ink">
            {error ?? accountsError}
          </div>
        )}

        <TransactionList transactions={transactions} onReverse={reverse} />
      </div>
    </div>
  );
}

export default App;
