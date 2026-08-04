import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { NavTabs } from "../NavTabs";

describe("NavTabs", () => {
  it("marks the active tab with aria-current", () => {
    render(<NavTabs active="dashboard" onChange={vi.fn()} />);

    expect(screen.getByRole("button", { name: "Dashboard" })).toHaveAttribute("aria-current", "page");
    expect(screen.getByRole("button", { name: "Transactions" })).not.toHaveAttribute("aria-current");
  });

  it("calls onChange with the clicked tab's view", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(<NavTabs active="transactions" onChange={onChange} />);

    await user.click(screen.getByRole("button", { name: "Dashboard" }));

    expect(onChange).toHaveBeenCalledWith("dashboard");
  });
});
