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

function renderRow(overrides: Partial<Transaction> = {}, props: Record<string, unknown> = {}) {
  return render(
    <table>
      <tbody>
        <TransactionRow
          transaction={buildTransaction(overrides)}
          onConfirm={vi.fn()}
          onReverse={vi.fn()}
          isConfirming={false}
          isReversing={false}
          {...props}
        />
      </tbody>
    </table>,
  );
}

describe("TransactionRow — Reverse action", () => {
  it("enables the Reverse button when status is POSTED", () => {
    renderRow({ status: "POSTED" });
    expect(screen.getByRole("button", { name: "Reverse" })).toBeEnabled();
  });

  it("disables the Reverse button when status is PENDING", () => {
    renderRow({ status: "PENDING" });
    expect(screen.getByRole("button", { name: "Reverse" })).toBeDisabled();
  });

  it("disables the Reverse button when status is already REVERSED", () => {
    renderRow({ status: "REVERSED" });
    expect(screen.getByRole("button", { name: "Reverse" })).toBeDisabled();
  });

  it("calls onReverse with the transaction id when clicked", async () => {
    const user = userEvent.setup();
    const onReverse = vi.fn();
    renderRow({ id: "txn-42", status: "POSTED" }, { onReverse });

    await user.click(screen.getByRole("button", { name: "Reverse" }));

    expect(onReverse).toHaveBeenCalledWith("txn-42");
  });

  it("shows a pending label and disables the button while reversing", () => {
    renderRow({ status: "POSTED" }, { isReversing: true });
    expect(screen.getByRole("button", { name: "Reversing…" })).toBeDisabled();
  });
});

describe("TransactionRow — Confirm action", () => {
  it("enables the Confirm button when status is PENDING", () => {
    renderRow({ status: "PENDING" });
    expect(screen.getByRole("button", { name: "Confirm" })).toBeEnabled();
  });

  it("disables the Confirm button when status is POSTED", () => {
    renderRow({ status: "POSTED" });
    expect(screen.getByRole("button", { name: "Confirm" })).toBeDisabled();
  });

  it("disables the Confirm button when status is REVERSED", () => {
    renderRow({ status: "REVERSED" });
    expect(screen.getByRole("button", { name: "Confirm" })).toBeDisabled();
  });

  it("calls onConfirm with the transaction id when clicked", async () => {
    const user = userEvent.setup();
    const onConfirm = vi.fn();
    renderRow({ id: "txn-42", status: "PENDING" }, { onConfirm });

    await user.click(screen.getByRole("button", { name: "Confirm" }));

    expect(onConfirm).toHaveBeenCalledWith("txn-42");
  });

  it("shows a pending label and disables the button while confirming", () => {
    renderRow({ status: "PENDING" }, { isConfirming: true });
    expect(screen.getByRole("button", { name: "Confirming…" })).toBeDisabled();
  });
});

describe("TransactionRow — formatting", () => {
  it("formats the amount as currency", () => {
    renderRow({ amount: 1234.5 });
    expect(screen.getByText("$1,234.50")).toBeInTheDocument();
  });
});
