import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { StatusFilter } from "../../components/StatusFilter";

describe("StatusFilter", () => {
  it("marks the currently selected option as pressed", () => {
    render(<StatusFilter value="POSTED" onChange={vi.fn()} />);

    expect(screen.getByRole("button", { name: "Posted" })).toHaveAttribute("aria-pressed", "true");
    expect(screen.getByRole("button", { name: "All" })).toHaveAttribute("aria-pressed", "false");
  });

  it("calls onChange with the clicked status", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(<StatusFilter value="ALL" onChange={onChange} />);

    await user.click(screen.getByRole("button", { name: "Pending" }));

    expect(onChange).toHaveBeenCalledWith("PENDING");
  });

  it("renders all four filter options", () => {
    render(<StatusFilter value="ALL" onChange={vi.fn()} />);

    expect(screen.getByRole("button", { name: "All" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Posted" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Pending" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Reversed" })).toBeInTheDocument();
  });
});
