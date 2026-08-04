import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { StatCard } from "../StatCard";

describe("StatCard", () => {
  it("renders the label and value", () => {
    render(<StatCard label="Total accounts" value="12" />);

    expect(screen.getByText("Total accounts")).toBeInTheDocument();
    expect(screen.getByText("12")).toBeInTheDocument();
  });
});
