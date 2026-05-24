import api from "@/lib/api";
import type {
    AppointmentResponse,
    AppointmentStatus,
    ConsultationNoteRequest,
    ConsultationNoteResponse,
    DoctorNoteListItem,
    DoctorPatientListItem,
    DoctorProfileResponse,
    MedicalDocument,
    PatientHistoryItem,
    PatientSummary,
    RecentPatient,
} from "@/types/api";

export const doctorApi = {
    getMyProfile: () =>
        api.get<DoctorProfileResponse>("/doctors/me").then((r) => r.data),

    updateMyProfile: (data: { biography?: string; consultationFee?: number }) =>
        api.put<DoctorProfileResponse>("/doctors/me", data).then((r) => r.data),

    getMyAppointments: () =>
        api.get<AppointmentResponse[]>("/appointments/my/doctor").then((r) => r.data),

    updateAppointmentStatus: (appointmentId: number, status: AppointmentStatus) =>
        api.patch<AppointmentResponse>(`/appointments/${appointmentId}/status`, { status }).then((r) => r.data),

    getRecentPatients: () =>
        api.get<RecentPatient[]>("/doctors/me/recent-patients").then((r) => r.data),

    getConsultationNote: (appointmentId: number) =>
        api.get<ConsultationNoteResponse>(`/consultation-notes/appointment/${appointmentId}`).then((r) => r.data),

    createConsultationNote: (appointmentId: number, data: ConsultationNoteRequest) =>
        api.post<ConsultationNoteResponse>(`/consultation-notes/appointment/${appointmentId}`, data).then((r) => r.data),

    updateConsultationNote: (appointmentId: number, data: ConsultationNoteRequest) =>
        api.put<ConsultationNoteResponse>(`/consultation-notes/appointment/${appointmentId}`, data).then((r) => r.data),

    getMyNotes: () =>
        api.get<DoctorNoteListItem[]>("/consultation-notes/mine").then((r) => r.data),

    getMyPatients: () =>
        api.get<DoctorPatientListItem[]>("/doctors/me/patients").then((r) => r.data),

    getPatientSummary: (patientProfileId: number) =>
        api.get<PatientSummary>(`/doctors/me/patients/${patientProfileId}`).then((r) => r.data),

    getPatientHistory: (patientProfileId: number) =>
        api.get<PatientHistoryItem[]>(`/consultation-notes/patient/${patientProfileId}`).then((r) => r.data),

    deleteDocument: (noteId: number, documentId: number) =>
        api.delete(`/consultation-notes/${noteId}/documents/${documentId}`),

    uploadDocument: (noteId: number, file: File) => {
        const form = new FormData();
        form.append("file", file);
        return api.post<MedicalDocument>(`/consultation-notes/${noteId}/documents`, form, {
            headers: { "Content-Type": "multipart/form-data" },
        }).then((r) => r.data);
    },

    downloadDocument: async (noteId: number, documentId: number, fileName: string) => {
        const response = await api.get(`/consultation-notes/${noteId}/documents/${documentId}`, {
            responseType: "blob",
        });
        const url = URL.createObjectURL(response.data as Blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    },
};