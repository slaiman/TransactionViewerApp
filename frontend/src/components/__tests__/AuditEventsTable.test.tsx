import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { AuditEventsTable } from "../AuditEventsTable";
import type { AuditEvent } from "../../types/audit";

function buildEvent(overrides: Partial<AuditEvent> = {}): AuditEvent {
  return {
    id: "evt-1",
    operation: "CREATE_TRANSACTION",
    entityType: "TRANSACTION",
    entityId: "txn-1",
    accountId: "acc-001",
    oldStatus: null,
    newStatus: "PENDING",
    timestamp: "2026-07-18T10:23:45.000Z",
    details: "Transaction created",
    ...overrides,
  };
}

describe("AuditEventsTable", () => {
  it("shows an empty state message when there are no events", () => {
    render(<AuditEventsTable events={[]} />);

    expect(screen.getByText("No audit events recorded yet.")).toBeInTheDocument();
  });

  it("renders one row per event", () => {
    render(
      <AuditEventsTable
        events={[buildEvent({ id: "evt-1" }), buildEvent({ id: "evt-2", entityId: "txn-2" })]}
      />,
    );

    expect(screen.getAllByRole("row")).toHaveLength(3); // 2 data rows + header row
  });

  it("shows just the new status for events with no prior status (e.g. creation)", () => {
    render(<AuditEventsTable events={[buildEvent({ oldStatus: null, newStatus: "PENDING" })]} />);

    expect(screen.getByText("PENDING")).toBeInTheDocument();
  });

  it("shows old and new status for a status-change event", () => {
    render(
      <AuditEventsTable
        events={[buildEvent({ operation: "TRANSACTION_STATUS_CHANGE", oldStatus: "PENDING", newStatus: "POSTED" })]}
      />,
    );

    expect(screen.getByText(/PENDING/)).toBeInTheDocument();
    expect(screen.getByText(/POSTED/)).toBeInTheDocument();
  });
});
