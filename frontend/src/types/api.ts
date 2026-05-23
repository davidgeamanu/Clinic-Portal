// ─── Enums ────────────────────────────────────────────────────────────────────

export type Role = "ADMIN" | "DOCTOR" | "PATIENT";

export type AppointmentStatus =
    | "SCHEDULED"
    | "CONFIRMED"
    | "IN_PROGRESS"
    | "COMPLETED"
    | "CANCELLED";

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
  avgConsultationMinutes: number | null;
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
  startedAt: string | null;
  completedAt: string | null;
  rating: number | null;
  review: string | null;
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

// ─── Consultation Notes ───────────────────────────────────────────────────────

export interface ConsultationNoteResponse {
  id: number;
  appointmentId: number;
  diagnosis: string;
  treatment: string | null;
  prescription: string | null;
  notes: string | null;
  createdAt: string;
}

export interface ConsultationNoteRequest {
  diagnosis: string;
  treatment?: string;
  prescription?: string;
  notes?: string;
}

// ─── Doctor Patient List ──────────────────────────────────────────────────────

export interface DoctorPatientListItem {
  patientProfileId: number;
  firstName: string;
  lastName: string;
  dateOfBirth: string | null;
  gender: string | null;
  bloodType: string | null;
  email: string;
  phoneNumber: string | null;
  lastVisitDate: string | null;
  lastDiagnosis: string | null;
  appointmentCount: number;
}

// ─── Doctor Note List ─────────────────────────────────────────────────────────

export interface DoctorNoteListItem {
  noteId: number;
  appointmentId: number;
  patientProfileId: number;
  patientName: string;
  appointmentDate: string;
  diagnosis: string;
  treatment: string | null;
  prescription: string | null;
  notes: string | null;
  documents: MedicalDocument[];
}

// ─── Medical Documents ────────────────────────────────────────────────────────

export interface MedicalDocument {
  id: number;
  originalFileName: string;
  contentType: string;
  fileSizeBytes: number;
  createdAt: string;
}

// ─── Patient History (all consultation notes across all doctors) ──────────────

export interface PatientHistoryItem {
  noteId: number;
  appointmentId: number;
  appointmentDate: string;
  doctorName: string;
  diagnosis: string;
  treatment: string | null;
  prescription: string | null;
  notes: string | null;
  documents: MedicalDocument[];
}

// ─── Patient Summary (doctor view) ───────────────────────────────────────────

export interface PatientSummary {
  patientProfileId: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string | null;
  dateOfBirth: string | null;
  gender: string | null;
  bloodType: string | null;
  emergencyContactName: string | null;
  emergencyContactPhone: string | null;
  heightCm: number | null;
  weightKg: number | null;
  allergies: string | null;
  chronicConditions: string | null;
  familyHistory: string | null;
  lifestyleSmoking: string | null;
  lifestyleAlcohol: string | null;
  lifestyleExercise: string | null;
  lifestyleDiet: string | null;
}

// ─── Doctor Dashboard ─────────────────────────────────────────────────────────

export interface RecentPatient {
  patientProfileId: number;
  firstName: string;
  lastName: string;
  lastVisitDate: string;
  diagnosis: string | null;
  notes: string | null;
}

// ─── Booked slots (for booking-flow availability check) ──────────────────────

export interface BookedSlot {
  scheduledAt: string;        // ISO LocalDateTime
  durationMinutes: number;
}

// ─── Patient Profile (self view) ──────────────────────────────────────────────

export interface PatientProfileResponse {
  id: number;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber: string | null;
  dateOfBirth: string | null;
  gender: string | null;
  bloodType: string | null;
  address: string | null;
  emergencyContactName: string | null;
  emergencyContactPhone: string | null;
  heightCm: number | null;
  weightKg: number | null;
  allergies: string | null;
  chronicConditions: string | null;
  familyHistory: string | null;
  lifestyleSmoking: string | null;
  lifestyleAlcohol: string | null;
  lifestyleExercise: string | null;
  lifestyleDiet: string | null;
}

export interface PatientProfileUpdateRequest {
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  gender?: string;
  bloodType?: string;
  address?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  heightCm?: number | null;
  weightKg?: number | null;
  allergies?: string;
  chronicConditions?: string;
  familyHistory?: string;
  lifestyleSmoking?: string;
  lifestyleAlcohol?: string;
  lifestyleExercise?: string;
  lifestyleDiet?: string;
}

// ─── Notifications ────────────────────────────────────────────────────────────

export type NotificationType =
    | "APPOINTMENT_SCHEDULED"
    | "APPOINTMENT_CONFIRMED"
    | "APPOINTMENT_STARTED"
    | "APPOINTMENT_CANCELLED"
    | "CONSULTATION_NOTE_ADDED"
    | "GENERAL";

export interface NotificationResponse {
  id: number;
  message: string;
  type: NotificationType;
  read: boolean;
  relatedEntityId: number | null;
  createdAt: string;
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