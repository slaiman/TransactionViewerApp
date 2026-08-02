import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { TransactionList } from "../../components/TransactionList";
import type { Transaction } from "../../types/transaction";

const sample: Transaction[] = [
  {
    id: "txn-1",
    accountId: "acc-001",
    date: "2026-07-18",
    merchantName: "Starbucks",
    amount: 6.75,
    status: "POSTED",
  },
  {
    id: "txn-2",
    accountId: "acc-001",
    date: "2026-07-10",
    merchantName: "Netflix",
    amount: 15.99,
    status: "PENDING",
  },
];

describe("TransactionList", () => {
  it("shows an empty state message when there are no transactions", () => {
    render(<TransactionList transactions={[]} onReverse={vi.fn()} />);

    expect(screen.getByText("No transactions match this filter.")).toBeInTheDocument();
  });

  it("renders one row per transaction", () => {
    render(<TransactionList transactions={sample} onReverse={vi.fn()} />);

    expect(screen.getByText("Starbucks")).toBeInTheDocument();
    expect(screen.getByText("Netflix")).toBeInTheDocument();
    expect(screen.getAllByRole("row")).toHaveLength(3); // 2 data rows + header row
  });
});
