import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { TransactionRow } from "../../components/TransactionRow";
import type { Transaction } from "../../types/transaction";

function buildTransaction(overrides: Partial<Transaction> = {}): Transaction {
  return {
    id: "txn-1",
    accountId: "acc-001",
    date: "2026-07-18",
    merchantName: "Whole Foods Market",
    amount: 84.23,
    status: "POSTED",
    ...overrides,
  };
}

describe("TransactionRow", () => {
  it("enables the Reverse button when status is POSTED", () => {
    render(
      <table>
        <tbody>
          <TransactionRow transaction={buildTransaction({ status: "POSTED" })} onReverse={vi.fn()} isReversing={false} />
        </tbody>
      </table>,
    );

    expect(screen.getByRole("button", { name: "Reverse" })).toBeEnabled();
  });

  it("disables the Reverse button when status is PENDING", () => {
    render(
      <table>
        <tbody>
          <TransactionRow transaction={buildTransaction({ status: "PENDING" })} onReverse={vi.fn()} isReversing={false} />
        </tbody>
      </table>,
    );

    expect(screen.getByRole("button", { name: "Reverse" })).toBeDisabled();
  });

  it("disables the Reverse button when status is already REVERSED", () => {
    render(
      <table>
        <tbody>
          <TransactionRow transaction={buildTransaction({ status: "REVERSED" })} onReverse={vi.fn()} isReversing={false} />
        </tbody>
      </table>,
    );

    expect(screen.getByRole("button", { name: "Reverse" })).toBeDisabled();
  });

  it("calls onReverse with the transaction id when clicked", async () => {
    const user = userEvent.setup();
    const onReverse = vi.fn();
    render(
      <table>
        <tbody>
          <TransactionRow
            transaction={buildTransaction({ id: "txn-42", status: "POSTED" })}
            onReverse={onReverse}
            isReversing={false}
          />
        </tbody>
      </table>,
    );

    await user.click(screen.getByRole("button", { name: "Reverse" }));

    expect(onReverse).toHaveBeenCalledWith("txn-42");
  });

  it("shows a pending label and disables the button while reversing", () => {
    render(
      <table>
        <tbody>
          <TransactionRow transaction={buildTransaction({ status: "POSTED" })} onReverse={vi.fn()} isReversing={true} />
        </tbody>
      </table>,
    );

    const button = screen.getByRole("button", { name: "Reversing…" });
    expect(button).toBeDisabled();
  });

  it("formats the amount as currency", () => {
    render(
      <table>
        <tbody>
          <TransactionRow transaction={buildTransaction({ amount: 1234.5 })} onReverse={vi.fn()} isReversing={false} />
        </tbody>
      </table>,
    );

    expect(screen.getByText("$1,234.50")).toBeInTheDocument();
  });
});
