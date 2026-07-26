import api from "@/lib/api";
import type {
  UserResponse,
  DoctorProfileResponse,
  AdminCreateDoctorRequest,
  SpecializationResponse,
  AppointmentResponse,
  AdminDashboard,
  AdminAnalytics,
  AdminDepartment,
  RoomResponse,
  RoomUpdateRequest,
  AdminPatient,
  AppointmentMode,
  AppointmentStatus,
  Page,
  Role,
} from "@/types/api";

export const adminApi = {
  // Dashboard & Analytics
  getDashboard: () =>
      api.get<AdminDashboard>("/admin/dashboard").then((r) => r.data),

  getAnalytics: () =>
      api.get<AdminAnalytics>("/admin/analytics").then((r) => r.data),

  // Departments
  getDepartments: () =>
      api.get<AdminDepartment[]>("/admin/departments").then((r) => r.data),

  // Rooms
  getRooms: () =>
      api.get<RoomResponse[]>("/admin/rooms").then((r) => r.data),

  updateRoom: (id: number, data: RoomUpdateRequest) =>
      api.patch<RoomResponse>(`/admin/rooms/${id}`, data).then((r) => r.data),

  // Patients
  // search/active are applied server-side across the whole table, not per page
  getAdminPatients: (page = 0, size = 20, search = "", active?: boolean) =>
      api.get<Page<AdminPatient>>("/admin/patients", {
        params: { page, size, search, active },
      }).then((r) => r.data),

  getPatientAppointments: (patientProfileId: number) =>
      api.get<AppointmentResponse[]>(`/admin/patients/${patientProfileId}/appointments`).then((r) => r.data),

  // Users
  getAllUsers: () =>
      api.get<UserResponse[]>("/admin/users").then((r) => r.data),

  getUsersByRole: (role: Role) =>
      api.get<UserResponse[]>(`/admin/users/role/${role}`).then((r) => r.data),

  setActiveStatus: (userId: number, active: boolean) =>
      api
          .patch<UserResponse>(`/admin/users/${userId}/status`, null, {
            params: { active },
          })
          .then((r) => r.data),

  // Doctors
  getAllDoctorProfiles: () =>
      api.get<DoctorProfileResponse[]>("/doctors").then((r) => r.data),

  getDoctorAppointments: (doctorProfileId: number) =>
      api.get<AppointmentResponse[]>(`/admin/doctors/${doctorProfileId}/appointments`).then((r) => r.data),

  createDoctor: (data: AdminCreateDoctorRequest) =>
      api.post<DoctorProfileResponse>("/admin/doctors", data).then((r) => r.data),

  assignDoctorRoom: (doctorProfileId: number, roomId: number | null) =>
      api.patch<DoctorProfileResponse>(`/admin/doctors/${doctorProfileId}/room`, { roomId }).then((r) => r.data),

  // Specializations
  getAllSpecializations: () =>
      api.get<SpecializationResponse[]>("/admin/specializations").then((r) => r.data),

  // Appointments
  // search/status/mode are applied server-side across the whole table, not per page
  getAllAppointments: (page = 0, size = 20, search = "", status?: AppointmentStatus, mode?: AppointmentMode) =>
      api.get<Page<AppointmentResponse>>("/admin/appointments", {
        params: { page, size, search, status, mode },
      }).then((r) => r.data),

  cancelAppointment: (id: number) =>
      api.patch<AppointmentResponse>(`/admin/appointments/${id}/cancel`).then((r) => r.data),
};