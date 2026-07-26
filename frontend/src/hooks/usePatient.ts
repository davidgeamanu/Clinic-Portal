import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { patientApi, type BookAppointmentRequest, type RateAppointmentRequest } from "@/api/patient";
import { queryKeys } from "@/lib/query-keys";
import { getApiErrorMessage } from "@/lib/api";
import type { AppointmentStatus, PatientProfileUpdateRequest } from "@/types/api";

export function usePatientProfile() {
  return useQuery({
    queryKey: queryKeys.patients.me(),
    queryFn: patientApi.getMyProfile,
  });
}

export function useUpdatePatientProfile() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: PatientProfileUpdateRequest) => patientApi.updateMyProfile(data),
    onSuccess: () => {
      toast.success("Profile updated.");
      qc.invalidateQueries({ queryKey: queryKeys.patients.me() });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to update profile."));
    },
  });
}

export function usePatientAppointments() {
  return useQuery({
    queryKey: queryKeys.appointments.mine(),
    queryFn: patientApi.getMyAppointments,
  });
}

export function useUpdatePatientAppointmentStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ appointmentId, status }: { appointmentId: number; status: AppointmentStatus }) =>
      patientApi.updateAppointmentStatus(appointmentId, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.appointments.mine() });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to update appointment."));
    },
  });
}

export function useMyNotifications() {
  return useQuery({
    queryKey: queryKeys.notifications.mine(),
    queryFn: patientApi.getMyNotifications,
  });
}

export function useMarkNotificationRead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (notificationId: number) => patientApi.markNotificationRead(notificationId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.notifications.mine() });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to mark notification as read."));
    },
  });
}

export function useMarkAllNotificationsRead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => patientApi.markAllNotificationsRead(),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.notifications.mine() });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to mark notifications as read."));
    },
  });
}

export function useDoctorsList() {
  return useQuery({
    queryKey: queryKeys.doctors.list(),
    queryFn: patientApi.getDoctors,
  });
}

export function useBookAppointment(onSuccess?: () => void) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: BookAppointmentRequest) => patientApi.bookAppointment(data),
    onSuccess: (_, vars) => {
      toast.success("Appointment booked.");
      qc.invalidateQueries({ queryKey: queryKeys.appointments.mine() });
      const isoDate = vars.scheduledAt.slice(0, 10);
      qc.invalidateQueries({ queryKey: queryKeys.doctors.bookedSlots(vars.doctorId, isoDate) });
      onSuccess?.();
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to book appointment."));
    },
  });
}

export function useBookedSlots(doctorId: number | null, isoDate: string | null) {
  return useQuery({
    queryKey: queryKeys.doctors.bookedSlots(doctorId ?? 0, isoDate ?? ""),
    queryFn: () => patientApi.getBookedSlots(doctorId!, isoDate!),
    enabled: doctorId !== null && isoDate !== null,
  });
}

export function useDoctorAvailability(doctorId: number | null) {
  return useQuery({
    queryKey: queryKeys.doctors.availability(doctorId ?? 0),
    queryFn: () => patientApi.getDoctorAvailability(doctorId!),
    enabled: doctorId !== null,
  });
}

export function useAvailableSlots(doctorId: number | null, isoDate: string | null, enabled = true) {
  return useQuery({
    queryKey: queryKeys.doctors.availableSlots(doctorId ?? 0, isoDate ?? ""),
    queryFn: () => patientApi.getAvailableSlots(doctorId!, isoDate!),
    enabled: enabled && doctorId !== null && isoDate !== null,
  });
}

export function useDoctorReviews(doctorId: number | null) {
  return useQuery({
    queryKey: queryKeys.doctors.reviews(doctorId ?? 0),
    queryFn: () => patientApi.getDoctorReviews(doctorId!),
    enabled: doctorId !== null,
  });
}

export function useMyMedicalRecords() {
  return useQuery({
    queryKey: queryKeys.patients.myMedicalRecords(),
    queryFn: patientApi.getMyMedicalRecords,
  });
}

export function useTriage() {
  return useMutation({
    mutationFn: (symptoms: string) => patientApi.triage(symptoms),
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to analyze symptoms."));
    },
  });
}

export function useRateAppointment(onSuccess?: () => void) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ appointmentId, data }: { appointmentId: number; data: RateAppointmentRequest }) =>
      patientApi.rateAppointment(appointmentId, data),
    onSuccess: () => {
      toast.success("Thanks for your feedback!");
      qc.invalidateQueries({ queryKey: queryKeys.appointments.mine() });
      qc.invalidateQueries({ queryKey: queryKeys.doctors.list() });
      onSuccess?.();
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to submit rating."));
    },
  });
}