import { useState } from "react";
import { useAccountManagement } from "../hooks/useAccountManagement";
import { AccountsTable } from "../components/AccountsTable";
import { AccountFormModal } from "../components/AccountFormModal";
import { DeleteAccountDialog } from "../components/DeleteAccountDialog";
import type { Account, AccountStatus } from "../types/account";

export function AccountsPage() {
  const { accounts, isLoading, error, create, update, remove } = useAccountManagement();

  const [formModalAccount, setFormModalAccount] = useState<Account | null | undefined>(undefined);
  // undefined = closed, null = create mode, Account = edit mode
  const [deleteTarget, setDeleteTarget] = useState<Account | null>(null);

  return (
    <div>
      <header className="mb-8 flex items-start justify-between">
        <div>
          <p className="font-mono text-xs uppercase tracking-widest text-ink/40">All accounts</p>
          <h1 className="mt-1 text-2xl font-semibold text-ink">Accounts</h1>
        </div>
        <button
          type="button"
          onClick={() => setFormModalAccount(null)}
          className="rounded bg-ink px-4 py-1.5 text-sm font-medium text-paper transition-opacity hover:opacity-90"
        >
          New account
        </button>
      </header>

      {error && (
        <div className="mb-4 rounded-md border border-status-reversed/30 bg-status-reversed/5 px-4 py-3 text-sm text-ink">
          {error}
        </div>
      )}

      {isLoading ? (
        <p className="text-sm text-ink/40">Loading…</p>
      ) : (
        <AccountsTable
          accounts={accounts}
          onEdit={(account) => setFormModalAccount(account)}
          onDelete={(account) => setDeleteTarget(account)}
        />
      )}

      {formModalAccount !== undefined && (
        <AccountFormModal
          account={formModalAccount ?? undefined}
          onClose={() => setFormModalAccount(undefined)}
          onSubmitCreate={(accountHolderName) => create({ accountHolderName })}
          onSubmitUpdate={(id, accountHolderName, status: AccountStatus) =>
            update(id, { accountHolderName, status })
          }
        />
      )}

      {deleteTarget && (
        <DeleteAccountDialog account={deleteTarget} onClose={() => setDeleteTarget(null)} onConfirm={remove} />
      )}
    </div>
  );
}
