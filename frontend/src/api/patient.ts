import api from "@/lib/api";
import type {
    AppointmentMode,
    AppointmentResponse,
    AppointmentStatus,
    AvailabilityWindow,
    AvailableSlot,
    BookedSlot,
    DoctorReview,
    DoctorProfileResponse,
    NotificationResponse,
    PatientHistoryItem,
    PatientProfileResponse,
    PatientProfileUpdateRequest,
    TriageResponse,
} from "@/types/api";

export interface BookAppointmentRequest {
    doctorId: number;
    scheduledAt: string;        // ISO LocalDateTime, e.g. "2026-05-20T10:00:00"
    durationMinutes: number;
    mode: AppointmentMode;
    reason?: string;
}

export interface RateAppointmentRequest {
    rating: number;             // 1-5
    review?: string;
}

export const patientApi = {
    getMyProfile: () =>
        api.get<PatientProfileResponse>("/patients/me").then((r) => r.data),

    updateMyProfile: (data: PatientProfileUpdateRequest) =>
        api.put<PatientProfileResponse>("/patients/me", data).then((r) => r.data),

    getMyAppointments: () =>
        api.get<AppointmentResponse[]>("/appointments/my/patient").then((r) => r.data),

    updateAppointmentStatus: (appointmentId: number, status: AppointmentStatus) =>
        api.patch<AppointmentResponse>(`/appointments/${appointmentId}/status`, { status }).then((r) => r.data),

    getMyNotifications: () =>
        api.get<NotificationResponse[]>("/notifications/me").then((r) => r.data),

    markNotificationRead: (notificationId: number) =>
        api.patch<void>(`/notifications/${notificationId}/read`),

    markAllNotificationsRead: () =>
        api.patch<void>("/notifications/read-all"),

    getDoctors: () =>
        api.get<DoctorProfileResponse[]>("/doctors").then((r) => r.data),

    bookAppointment: (data: BookAppointmentRequest) =>
        api.post<AppointmentResponse>("/appointments", data).then((r) => r.data),

    getBookedSlots: (doctorId: number, isoDate: string) =>
        api.get<BookedSlot[]>(`/doctors/${doctorId}/booked-slots`, { params: { date: isoDate } }).then((r) => r.data),

    getDoctorAvailability: (doctorId: number) =>
        api.get<AvailabilityWindow[]>(`/doctors/${doctorId}/availability`).then((r) => r.data),

    getDoctorReviews: (doctorId: number) =>
        api.get<DoctorReview[]>(`/doctors/${doctorId}/reviews`).then((r) => r.data),

    getAvailableSlots: (doctorId: number, isoDate: string, durationMinutes = 30) =>
        api.get<AvailableSlot[]>(`/doctors/${doctorId}/available-slots`, {
            params: { date: isoDate, durationMinutes },
        }).then((r) => r.data),

    rateAppointment: (appointmentId: number, data: RateAppointmentRequest) =>
        api.post<AppointmentResponse>(`/appointments/${appointmentId}/rate`, data).then((r) => r.data),

    getMyMedicalRecords: () =>
        api.get<PatientHistoryItem[]>("/consultation-notes/my-history").then((r) => r.data),

    triage: (symptoms: string) =>
        api.post<TriageResponse>("/triage", { symptoms }).then((r) => r.data),
};