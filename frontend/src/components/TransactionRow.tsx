import type { Transaction } from "../types/transaction";
import { StatusBadge } from "./StatusBadge";

interface TransactionRowProps {
  transaction: Transaction;
  onReverse: (id: string) => void;
  isReversing: boolean;
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

export function TransactionRow({ transaction, onReverse, isReversing }: TransactionRowProps) {
  const canReverse = transaction.status === "POSTED";

  return (
    <tr className="border-b border-ink/5 last:border-0">
      <td className="whitespace-nowrap py-3 pr-4 font-mono text-sm text-ink/70">
        {dateFormatter.format(new Date(transaction.date))}
      </td>
      <td className="py-3 pr-4 text-sm text-ink">{transaction.merchantName}</td>
      <td className="py-3 pr-4 text-right font-mono text-sm tabular-nums text-ink">
        {currencyFormatter.format(transaction.amount)}
      </td>
      <td className="py-3 pr-4">
        <StatusBadge status={transaction.status} />
      </td>
      <td className="py-3 text-right">
        <button
          type="button"
          disabled={!canReverse || isReversing}
          onClick={() => onReverse(transaction.id)}
          className="rounded border border-ink/15 px-2.5 py-1 text-xs font-medium text-ink/70 transition-colors hover:enabled:border-accent hover:enabled:text-accent disabled:cursor-not-allowed disabled:opacity-30 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
        >
          {isReversing ? "Reversing…" : "Reverse"}
        </button>
      </td>
    </tr>
  );
}
