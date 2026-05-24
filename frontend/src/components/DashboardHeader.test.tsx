import { render, screen, fireEvent } from "@testing-library/react";
import { DashboardHeader } from "./DashboardHeader";
import { describe, it, expect, vi, beforeEach } from "vitest";

const mockNavigate = vi.fn();
vi.mock("react-router-dom", () => ({
  useNavigate: () => mockNavigate,
}));

vi.mock("@/contexts/RoleContext", () => ({
  useRole: () => ({
    user: { userId: 1, email: "patient@test.com", role: "PATIENT" },
  }),
}));

const mockNotifications = [
  { id: 1, message: "test", type: "APPOINTMENT_SCHEDULED", read: false, relatedEntityId: 1, createdAt: "2026-06-01T10:00:00" },
  { id: 2, message: "test2", type: "GENERAL", read: true, relatedEntityId: null, createdAt: "2026-06-01T09:00:00" },
];

vi.mock("@/hooks/usePatient", () => ({
  useMyNotifications: () => ({ data: mockNotifications }),
}));

describe("DashboardHeader", () => {
  beforeEach(() => {
    mockNavigate.mockClear();
  });

  it("renders title and subtitle", () => {
    render(<DashboardHeader title="My Dashboard" subtitle="Welcome" />);

    expect(screen.getByText("My Dashboard")).toBeInTheDocument();
    expect(screen.getByText("Welcome")).toBeInTheDocument();
  });

  it("shows unread badge count", () => {
    render(<DashboardHeader />);

    expect(screen.getByText("1")).toBeInTheDocument();
  });

  it("navigates to patient notifications on bell click", () => {
    render(<DashboardHeader />);

    const buttons = screen.getAllByRole("button");
    const bellButton = buttons.find(b => b.querySelector(".lucide-bell"));
    expect(bellButton).toBeTruthy();
    fireEvent.click(bellButton!);

    expect(mockNavigate).toHaveBeenCalledWith("/patient/notifications");
  });

  it("shows user initials in avatar", () => {
    render(<DashboardHeader />);

    expect(screen.getByText("PA")).toBeInTheDocument();
  });
});
