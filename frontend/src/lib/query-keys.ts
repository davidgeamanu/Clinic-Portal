import type { Role } from "@/types/api";

/**
 * Centralised query-key factory.
 * Using functions (not plain strings) lets us invalidate at any granularity:
 *   queryKeys.admin.users() -> invalidates every users query
 *   queryKeys.admin.usersByRole('DOCTOR') -> invalidates only the DOCTOR list
 */
export const queryKeys = {
  admin: {
    all: ["admin"] as const,
    dashboard: () => ["admin", "dashboard"] as const,
    analytics: () => ["admin", "analytics"] as const,
    departments: () => ["admin", "departments"] as const,
    rooms: () => ["admin", "rooms"] as const,
    patients: () => ["admin", "patients"] as const,
    patientAppointments: (id: number) => ["admin", "patients", id, "appointments"] as const,
    doctorProfiles: () => ["admin", "doctorProfiles"] as const,
    doctorAppointments: (id: number) => ["admin", "doctors", id, "appointments"] as const,
    users: () => ["admin", "users"] as const,
    usersByRole: (role: Role) => ["admin", "users", role] as const,
    specializations: () => ["admin", "specializations"] as const,
    appointments: () => ["admin", "appointments"] as const,
  },
  doctors: {
    all: ["doctors"] as const,
    list: () => ["doctors", "list"] as const,
    detail: (id: number) => ["doctors", id] as const,
    bySpecialization: (id: number) => ["doctors", "specialization", id] as const,
  },
  patients: {
    all: ["patients"] as const,
    me: () => ["patients", "me"] as const,
  },
  appointments: {
    all: ["appointments"] as const,
    mine: () => ["appointments", "me"] as const,
    detail: (id: number) => ["appointments", id] as const,
  },
  notifications: {
    all: ["notifications"] as const,
    mine: () => ["notifications", "me"] as const,
  },
} as const;