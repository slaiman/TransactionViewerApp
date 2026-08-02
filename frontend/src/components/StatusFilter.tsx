import type { TransactionStatus } from "../types/transaction";

interface StatusFilterProps {
  value: TransactionStatus | "ALL";
  onChange: (value: TransactionStatus | "ALL") => void;
}

const OPTIONS: Array<{ value: TransactionStatus | "ALL"; label: string }> = [
  { value: "ALL", label: "All" },
  { value: "POSTED", label: "Posted" },
  { value: "PENDING", label: "Pending" },
  { value: "REVERSED", label: "Reversed" },
];

export function StatusFilter({ value, onChange }: StatusFilterProps) {
  return (
    <div className="inline-flex rounded-md border border-ink/10 bg-white p-1" role="group" aria-label="Filter by status">
      {OPTIONS.map((option) => {
        const isActive = option.value === value;
        return (
          <button
            key={option.value}
            type="button"
            onClick={() => onChange(option.value)}
            className={`rounded px-3 py-1.5 text-sm font-medium transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent ${
              isActive
                ? "bg-ink text-paper"
                : "text-ink/60 hover:text-ink"
            }`}
            aria-pressed={isActive}
          >
            {option.label}
          </button>
        );
      })}
    </div>
  );
}
