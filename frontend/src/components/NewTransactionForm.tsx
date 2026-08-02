import { useState, type FormEvent } from "react";
import type { CreateTransactionRequest } from "../types/transaction";

interface NewTransactionFormProps {
  accountId: string;
  onCreate: (request: CreateTransactionRequest) => Promise<void>;
}

export function NewTransactionForm({ accountId, onCreate }: NewTransactionFormProps) {
  const [merchantName, setMerchantName] = useState("");
  const [amount, setAmount] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    const parsedAmount = Number(amount);
    if (!merchantName.trim() || !parsedAmount || parsedAmount <= 0) return;

    setIsSubmitting(true);
    await onCreate({ accountId, merchantName: merchantName.trim(), amount: parsedAmount });
    setIsSubmitting(false);
    setMerchantName("");
    setAmount("");
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-wrap items-end gap-3 rounded-md border border-ink/10 bg-white p-4">
      <div className="flex flex-col gap-1">
        <label htmlFor="merchantName" className="text-xs font-medium uppercase tracking-wide text-ink/40">
          Merchant
        </label>
        <input
          id="merchantName"
          type="text"
          value={merchantName}
          onChange={(e) => setMerchantName(e.target.value)}
          placeholder="e.g. Trader Joe's"
          className="w-48 rounded border border-ink/15 px-2.5 py-1.5 text-sm text-ink placeholder:text-ink/30 focus:border-accent focus:outline-none"
        />
      </div>
      <div className="flex flex-col gap-1">
        <label htmlFor="amount" className="text-xs font-medium uppercase tracking-wide text-ink/40">
          Amount
        </label>
        <input
          id="amount"
          type="number"
          step="0.01"
          min="0.01"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          placeholder="0.00"
          className="w-28 rounded border border-ink/15 px-2.5 py-1.5 font-mono text-sm text-ink placeholder:text-ink/30 focus:border-accent focus:outline-none"
        />
      </div>
      <button
        type="submit"
        disabled={isSubmitting}
        className="rounded bg-ink px-4 py-1.5 text-sm font-medium text-paper transition-opacity hover:opacity-90 disabled:opacity-50"
      >
        {isSubmitting ? "Adding…" : "Simulate purchase"}
      </button>
    </form>
  );
}
