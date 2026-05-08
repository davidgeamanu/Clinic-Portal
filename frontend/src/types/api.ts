// ─── Enums ────────────────────────────────────────────────────────────────────

export type Role = "ADMIN" | "DOCTOR" | "PATIENT";

export type AppointmentStatus =
  | "SCHEDULED"
  | "CONFIRMED"
  | "COMPLETED"
  | "CANCELLED"
  | "RESCHEDULED";

export type AppointmentMode = "IN_PERSON" | "VIDEO";

// ─── Auth ─────────────────────────────────────────────────────────────────────

export interface AuthResponse {
  role: Role;
  userId: number;
  email: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
}

// ─── User ─────────────────────────────────────────────────────────────────────

export interface UserResponse {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  role: Role;
  active: boolean;
}

// ─── Specialization ───────────────────────────────────────────────────────────

export interface SpecializationResponse {
  id: number;
  name: string;
  description: string | null;
}

export interface SpecializationRequest {
  name: string;
  description?: string;
}

// ─── Doctor ───────────────────────────────────────────────────────────────────

export interface DoctorProfileResponse {
  id: number;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  active: boolean;
  licenseNumber: string;
  biography: string | null;
  consultationFee: number;
  rating: number | null;
  roomId: number | null;
  roomNumber: string | null;
  specializations: SpecializationResponse[];
}

export interface AdminCreateDoctorRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  licenseNumber: string;
  biography?: string;
  specializationIds?: number[];
}

// ─── Appointment ──────────────────────────────────────────────────────────────

export interface AppointmentResponse {
  id: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  scheduledAt: string; // ISO-8601 LocalDateTime from backend
  durationMinutes: number;
  mode: AppointmentMode;
  roomNumber: string | null;
  roomType: RoomType | null;
  roomDepartment: string | null;
  status: AppointmentStatus;
  reason: string | null;
  createdAt: string;
}

// ─── Room ─────────────────────────────────────────────────────────────────────

export type RoomType = "CONSULT" | "OR" | "IMAGING";
export type RoomStatus = "FREE" | "OCCUPIED";

export interface RoomResponse {
  id: number;
  roomNumber: string;
  floor: number;
  type: RoomType;
  status: RoomStatus;
  specializationId: number | null;
  specializationName: string | null;
  assignedDoctorId: number | null;
  assignedDoctorName: string | null;
}

export interface RoomUpdateRequest {
  specializationId: number | null;
  type: RoomType;
  status: RoomStatus;
}

// ─── Admin Patient ────────────────────────────────────────────────────────────

export interface AdminPatient {
  patientProfileId: number;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string | null;
  active: boolean;
  dateOfBirth: string | null;
  gender: string | null;
  bloodType: string | null;
  address: string | null;
  emergencyContactName: string | null;
  emergencyContactPhone: string | null;
}

// ─── Admin Department ─────────────────────────────────────────────────────────

export interface AdminDepartment {
  id: number;
  name: string;
  description: string | null;
  doctorCount: number;
  consultRoomCount: number;
  orRoomCount: number;
  imagingRoomCount: number;
}

// ─── Admin Dashboard ──────────────────────────────────────────────────────────

export interface DailyStats {
  day: string;
  patients: number;
  appointments: number;
  revenue: number;
}

export interface DepartmentLoad {
  name: string;
  value: number;
}

export interface AdminDashboard {
  totalPatients: number;
  activeDoctors: number;
  todaysAppointments: number;
  monthlyRevenue: number;
  weeklyStats: DailyStats[];
  departmentLoad: DepartmentLoad[];
}

// ─── Admin Analytics ──────────────────────────────────────────────────────────

export interface MonthlyRevenue {
  month: string;
  revenue: number;
}

export interface MonthlyPatientCount {
  month: string;
  count: number;
}

export interface AdminAnalytics {
  totalRevenue: number;
  totalPatients: number;
  monthlyRevenue: MonthlyRevenue[];
  patientTrend: MonthlyPatientCount[];
}

// ─── API Error (mirrors ExceptionBody) ────────────────────────────────────────

export interface ApiError {
  timestamp: string;
  code: string;
  message: string;
  details: Record<string, string>;
}