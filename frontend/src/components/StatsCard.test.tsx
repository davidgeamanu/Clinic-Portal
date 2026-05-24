import { render, screen } from "@testing-library/react";
import { StatsCard } from "./StatsCard";
import { Calendar } from "lucide-react";
import { describe, it, expect } from "vitest";

describe("StatsCard", () => {
  it("renders title and value", () => {
    render(
      <StatsCard
        title="Total Appointments"
        value="42"
        icon={Calendar}
        iconBg="bg-primary/10 text-primary"
      />
    );

    expect(screen.getByText("Total Appointments")).toBeInTheDocument();
    expect(screen.getByText("42")).toBeInTheDocument();
  });

  it("renders change text when provided", () => {
    render(
      <StatsCard
        title="Revenue"
        value="$1,200"
        change="+12% this month"
        changeType="positive"
        icon={Calendar}
        iconBg="bg-success/10"
      />
    );

    expect(screen.getByText("+12% this month")).toBeInTheDocument();
  });

  it("does not render change text when omitted", () => {
    render(
      <StatsCard
        title="Patients"
        value="100"
        icon={Calendar}
        iconBg="bg-info/10"
      />
    );

    expect(screen.queryByText(/this month/)).not.toBeInTheDocument();
  });
});
