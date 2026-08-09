import { useState } from "react";
import type { Transaction } from "../types/transaction";
import { TransactionRow } from "./TransactionRow";

interface TransactionListProps {
  transactions: Transaction[];
  onConfirm: (id: string) => Promise<void>;
  onReverse: (id: string) => Promise<void>;
}

export function TransactionList({ transactions, onConfirm, onReverse }: TransactionListProps) {
  const [confirmingId, setConfirmingId] = useState<string | null>(null);
  const [reversingId, setReversingId] = useState<string | null>(null);

  const handleConfirm = async (id: string) => {
    setConfirmingId(id);
    await onConfirm(id);
    setConfirmingId(null);
  };

  const handleReverse = async (id: string) => {
    setReversingId(id);
    await onReverse(id);
    setReversingId(null);
  };

  if (transactions.length === 0) {
    return (
      <div className="rounded-md border border-dashed border-ink/15 py-16 text-center">
        <p className="text-sm text-ink/50">No transactions match this filter.</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-md border border-ink/10 bg-white">
      <table className="w-full min-w-[640px] border-collapse">
        <thead>
          <tr className="border-b border-ink/10 text-left text-xs uppercase tracking-wide text-ink/40">
            <th className="py-3 pr-4 font-medium">Date</th>
            <th className="py-3 pr-4 font-medium">Merchant</th>
            <th className="py-3 pr-4 text-right font-medium">Amount</th>
            <th className="py-3 pr-4 font-medium">Status</th>
            <th className="py-3 font-medium" />
          </tr>
        </thead>
        <tbody>
          {transactions.map((transaction) => (
            <TransactionRow
              key={transaction.id}
              transaction={transaction}
              onConfirm={handleConfirm}
              onReverse={handleReverse}
              isConfirming={confirmingId === transaction.id}
              isReversing={reversingId === transaction.id}
            />
          ))}
        </tbody>
      </table>
    </div>
  );
}
