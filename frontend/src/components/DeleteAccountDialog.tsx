import { useState } from "react";
import type { Account } from "../types/account";

interface DeleteAccountDialogProps {
  account: Account;
  onClose: () => void;
  onConfirm: (id: string) => Promise<boolean>;
}

export function DeleteAccountDialog({ account, onClose, onConfirm }: DeleteAccountDialogProps) {
  const [isDeleting, setIsDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleConfirm = async () => {
    setIsDeleting(true);
    setError(null);
    const success = await onConfirm(account.id);
    setIsDeleting(false);
    if (success) {
      onClose();
    } else {
      setError(
        "This account couldn't be deleted — it most likely still has transaction history. " +
          "Consider closing it instead of deleting it.",
      );
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/30 px-4" role="dialog" aria-modal="true">
      <div className="w-full max-w-sm rounded-md bg-white p-6 shadow-lg">
        <h2 className="text-lg font-semibold text-ink">Delete account</h2>
        <p className="mt-2 text-sm text-ink/70">
          Delete <span className="font-medium text-ink">{account.accountHolderName}</span> ({account.id})? This
          can't be undone.
        </p>

        {error && (
          <div className="mt-3 rounded-md border border-status-reversed/30 bg-status-reversed/5 px-3 py-2 text-sm text-ink">
            {error}
          </div>
        )}

        <div className="mt-5 flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="rounded px-3 py-1.5 text-sm font-medium text-ink/60 transition-colors hover:text-ink"
          >
            Cancel
          </button>
          <button
            type="button"
            disabled={isDeleting}
            onClick={handleConfirm}
            className="rounded bg-status-reversed px-4 py-1.5 text-sm font-medium text-white transition-opacity hover:opacity-90 disabled:opacity-50"
          >
            {isDeleting ? "Deleting…" : "Delete"}
          </button>
        </div>
      </div>
    </div>
  );
}
