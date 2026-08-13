import { useState, type FormEvent } from "react";
import type { Account, AccountStatus } from "../types/account";

interface AccountFormModalProps {
  account?: Account; // undefined = create mode, present = edit mode
  onClose: () => void;
  onSubmitCreate: (accountHolderName: string) => Promise<boolean>;
  onSubmitUpdate: (id: string, accountHolderName: string, status: AccountStatus) => Promise<boolean>;
}

export function AccountFormModal({ account, onClose, onSubmitCreate, onSubmitUpdate }: AccountFormModalProps) {
  const isEditMode = account !== undefined;
  const [accountHolderName, setAccountHolderName] = useState(account?.accountHolderName ?? "");
  const [status, setStatus] = useState<AccountStatus>(account?.status ?? "ACTIVE");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!accountHolderName.trim()) return;

    setIsSubmitting(true);
    const success = isEditMode
      ? await onSubmitUpdate(account.id, accountHolderName.trim(), status)
      : await onSubmitCreate(accountHolderName.trim());
    setIsSubmitting(false);

    if (success) onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/30 px-4" role="dialog" aria-modal="true">
      <div className="w-full max-w-sm rounded-md bg-white p-6 shadow-lg">
        <h2 className="text-lg font-semibold text-ink">{isEditMode ? "Edit account" : "New account"}</h2>

        <form onSubmit={handleSubmit} className="mt-4 flex flex-col gap-4">
          <div className="flex flex-col gap-1">
            <label htmlFor="accountHolderName" className="text-xs font-medium uppercase tracking-wide text-ink/40">
              Account holder
            </label>
            <input
              id="accountHolderName"
              type="text"
              value={accountHolderName}
              onChange={(e) => setAccountHolderName(e.target.value)}
              placeholder="e.g. Jordan Lee"
              autoFocus
              className="rounded border border-ink/15 px-2.5 py-1.5 text-sm text-ink placeholder:text-ink/30 focus:border-accent focus:outline-none"
            />
          </div>

          {isEditMode && (
            <div className="flex flex-col gap-1">
              <label htmlFor="status" className="text-xs font-medium uppercase tracking-wide text-ink/40">
                Status
              </label>
              <select
                id="status"
                value={status}
                onChange={(e) => setStatus(e.target.value as AccountStatus)}
                className="rounded border border-ink/15 px-2.5 py-1.5 text-sm text-ink focus:border-accent focus:outline-none"
              >
                <option value="ACTIVE">Active</option>
                <option value="CLOSED">Closed</option>
              </select>
            </div>
          )}

          <div className="mt-2 flex justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded px-3 py-1.5 text-sm font-medium text-ink/60 transition-colors hover:text-ink"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting || !accountHolderName.trim()}
              className="rounded bg-ink px-4 py-1.5 text-sm font-medium text-paper transition-opacity hover:opacity-90 disabled:opacity-50"
            >
              {isSubmitting ? "Saving…" : isEditMode ? "Save changes" : "Create account"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
