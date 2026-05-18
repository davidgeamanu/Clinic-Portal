import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { doctorApi } from "@/api/doctor";
import { queryKeys } from "@/lib/query-keys";
import { getApiErrorMessage } from "@/lib/api";
import type { AppointmentStatus, ConsultationNoteRequest } from "@/types/api";

export function useDoctorProfile() {
  return useQuery({
    queryKey: queryKeys.doctor.me(),
    queryFn: doctorApi.getMyProfile,
  });
}

export function useDoctorAppointments() {
  return useQuery({
    queryKey: queryKeys.appointments.mine(),
    queryFn: doctorApi.getMyAppointments,
  });
}

export function useUpdateAppointmentStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ appointmentId, status }: { appointmentId: number; status: AppointmentStatus }) =>
      doctorApi.updateAppointmentStatus(appointmentId, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.appointments.mine() });
      qc.invalidateQueries({ queryKey: queryKeys.doctor.me() });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to update appointment status."));
    },
  });
}

export function useRecentPatients() {
  return useQuery({
    queryKey: queryKeys.doctor.recentPatients(),
    queryFn: doctorApi.getRecentPatients,
  });
}

export function useConsultationNote(appointmentId: number | null) {
  return useQuery({
    queryKey: queryKeys.doctor.consultationNote(appointmentId ?? 0),
    queryFn: () => doctorApi.getConsultationNote(appointmentId!),
    enabled: appointmentId !== null,
    retry: false,
  });
}

export function useCreateConsultationNote(appointmentId: number, onSuccess?: () => void) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: ConsultationNoteRequest) =>
      doctorApi.createConsultationNote(appointmentId, data),
    onSuccess: () => {
      toast.success("Notes saved.");
      qc.invalidateQueries({ queryKey: queryKeys.doctor.consultationNote(appointmentId) });
      qc.invalidateQueries({ queryKey: queryKeys.doctor.recentPatients() });
      onSuccess?.();
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to save notes."));
    },
  });
}

export function useUpdateConsultationNote(appointmentId: number, onSuccess?: () => void) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: ConsultationNoteRequest) =>
      doctorApi.updateConsultationNote(appointmentId, data),
    onSuccess: () => {
      toast.success("Notes updated.");
      qc.invalidateQueries({ queryKey: queryKeys.doctor.consultationNote(appointmentId) });
      qc.invalidateQueries({ queryKey: queryKeys.doctor.recentPatients() });
      onSuccess?.();
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to update notes."));
    },
  });
}

export function useDeleteDocument(noteId: number, patientProfileId: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (documentId: number) => doctorApi.deleteDocument(noteId, documentId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.doctor.notes() });
      qc.invalidateQueries({ queryKey: queryKeys.doctor.patientHistory(patientProfileId) });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to delete document."));
    },
  });
}

export function useUploadDocument(noteId: number, patientProfileId: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (file: File) => doctorApi.uploadDocument(noteId, file),
    onSuccess: () => {
      toast.success("File uploaded.");
      qc.invalidateQueries({ queryKey: queryKeys.doctor.notes() });
      qc.invalidateQueries({ queryKey: queryKeys.doctor.patientHistory(patientProfileId) });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to upload file."));
    },
  });
}

export function useMyNotes() {
  return useQuery({
    queryKey: queryKeys.doctor.notes(),
    queryFn: doctorApi.getMyNotes,
  });
}

export function useMyPatients() {
  return useQuery({
    queryKey: queryKeys.doctor.patients(),
    queryFn: doctorApi.getMyPatients,
  });
}

export function usePatientHistory(patientProfileId: number | null) {
  return useQuery({
    queryKey: queryKeys.doctor.patientHistory(patientProfileId ?? 0),
    queryFn: () => doctorApi.getPatientHistory(patientProfileId!),
    enabled: patientProfileId !== null,
  });
}

export function usePatientSummary(patientProfileId: number | null) {
  return useQuery({
    queryKey: queryKeys.doctor.patientSummary(patientProfileId ?? 0),
    queryFn: () => doctorApi.getPatientSummary(patientProfileId!),
    enabled: patientProfileId !== null,
  });
}