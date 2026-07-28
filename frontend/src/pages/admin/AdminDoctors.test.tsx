import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, it, expect, vi } from "vitest";
import AdminDoctors from "./AdminDoctors";
import type { DoctorProfileResponse, SpecializationResponse } from "@/types/api";

// All 7 are seeded by DataSeeder, but only Cardiology has anyone in it.
const SPECIALIZATIONS: SpecializationResponse[] = [
  { id: 1, name: "Cardiology", description: null },
  { id: 2, name: "Neurology", description: null },
];

const DOCTORS = [
  {
    id: 1, userId: 1, firstName: "Ada", lastName: "Byron",
    email: "ada@clinic.test", phoneNumber: null, licenseNumber: "L1",
    biography: null, consultationFee: 100, rating: null, active: true,
    room: null, specializations: [SPECIALIZATIONS[0]],
  },
  {
    id: 2, userId: 2, firstName: "Alan", lastName: "Turing",
    email: "alan@clinic.test", phoneNumber: null, licenseNumber: "L2",
    biography: null, consultationFee: 120, rating: null, active: true,
    room: null, specializations: [SPECIALIZATIONS[0]],
  },
] as unknown as DoctorProfileResponse[];

vi.mock("@/components/RolePageShell", () => ({
  RolePageShell: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

vi.mock("@/hooks/useAdmin", () => ({
  useAllDoctorProfiles: () => ({ data: DOCTORS, isLoading: false }),
  useSpecializations: () => ({ data: SPECIALIZATIONS }),
  useCreateDoctor: () => ({ mutate: vi.fn(), isPending: false }),
  useToggleUserStatus: () => ({ mutate: vi.fn(), isPending: false }),
  useDoctorAppointments: () => ({ data: [], isLoading: false }),
}));

function renderAt(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <AdminDoctors />
    </MemoryRouter>,
  );
}

describe("AdminDoctors ?dept= deep link", () => {
  it("filters to the requested department", () => {
    renderAt("/admin/doctors?dept=Cardiology");

    expect(screen.getByText("Ada Byron")).toBeInTheDocument();
    expect(screen.getByText("Alan Turing")).toBeInTheDocument();
  });

  it("shows no doctors for a department nobody is assigned to", () => {
    // Regression: `departments` was derived from the loaded doctors, so an empty
    // department failed the guard, the filter stayed "all", and every doctor showed.
    renderAt("/admin/doctors?dept=Neurology");

    expect(screen.queryByText("Ada Byron")).not.toBeInTheDocument();
    expect(screen.queryByText("Alan Turing")).not.toBeInTheDocument();
    expect(screen.getByText("No doctors match your search")).toBeInTheDocument();
  });

  it("lists every seeded department in the filter, not just the populated ones", () => {
    renderAt("/admin/doctors");

    expect(screen.getByText("Ada Byron")).toBeInTheDocument();
    expect(screen.getByText("Alan Turing")).toBeInTheDocument();
  });
});
