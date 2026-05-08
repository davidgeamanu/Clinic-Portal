import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { adminApi } from "@/api/admin";
import { queryKeys } from "@/lib/query-keys";
import { getApiErrorMessage } from "@/lib/api";
import type { AdminCreateDoctorRequest, Role, RoomUpdateRequest } from "@/types/api";

export function useAdminDepartments() {
  return useQuery({
    queryKey: queryKeys.admin.departments(),
    queryFn: adminApi.getDepartments,
  });
}

export function useAdminRooms() {
  return useQuery({
    queryKey: queryKeys.admin.rooms(),
    queryFn: adminApi.getRooms,
  });
}

export function useUpdateRoom() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: RoomUpdateRequest }) =>
      adminApi.updateRoom(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.admin.rooms() });
      qc.invalidateQueries({ queryKey: queryKeys.admin.departments() });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to update room."));
    },
  });
}

export function useAdminPatients() {
  return useQuery({
    queryKey: queryKeys.admin.patients(),
    queryFn: adminApi.getAdminPatients,
  });
}

export function usePatientAppointments(patientProfileId: number) {
  return useQuery({
    queryKey: queryKeys.admin.patientAppointments(patientProfileId),
    queryFn: () => adminApi.getPatientAppointments(patientProfileId),
    enabled: patientProfileId > 0,
  });
}

export function useAllDoctorProfiles() {
  return useQuery({
    queryKey: queryKeys.admin.doctorProfiles(),
    queryFn: adminApi.getAllDoctorProfiles,
  });
}

export function useDoctorAppointments(doctorProfileId: number) {
  return useQuery({
    queryKey: queryKeys.admin.doctorAppointments(doctorProfileId),
    queryFn: () => adminApi.getDoctorAppointments(doctorProfileId),
    enabled: doctorProfileId > 0,
  });
}

export function useAdminDashboard() {
  return useQuery({
    queryKey: queryKeys.admin.dashboard(),
    queryFn: adminApi.getDashboard,
  });
}

export function useAdminAnalytics() {
  return useQuery({
    queryKey: queryKeys.admin.analytics(),
    queryFn: adminApi.getAnalytics,
  });
}

export function useAllAppointments() {
  return useQuery({
    queryKey: queryKeys.admin.appointments(),
    queryFn: adminApi.getAllAppointments,
  });
}

export function useCancelAppointment() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => adminApi.cancelAppointment(id),
    onSuccess: () => {
      toast.success("Appointment cancelled.");
      qc.invalidateQueries({ queryKey: queryKeys.admin.appointments() });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to cancel appointment."));
    },
  });
}

export function useUsersByRole(role: Role) {
  return useQuery({
    queryKey: queryKeys.admin.usersByRole(role),
    queryFn: () => adminApi.getUsersByRole(role),
  });
}

export function useToggleUserStatus(onSuccess?: () => void) {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, active }: { userId: number; active: boolean }) =>
      adminApi.setActiveStatus(userId, active),
    onSuccess: (_, { active }) => {
      toast.success(`User ${active ? "activated" : "deactivated"}.`);
      qc.invalidateQueries({ queryKey: queryKeys.admin.users() });
      qc.invalidateQueries({ queryKey: queryKeys.admin.doctorProfiles() });
      qc.invalidateQueries({ queryKey: queryKeys.admin.patients() });
      onSuccess?.();
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to update user status."));
    },
  });
}

export function useCreateDoctor(onSuccess?: () => void) {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (data: AdminCreateDoctorRequest) => adminApi.createDoctor(data),
    onSuccess: () => {
      toast.success("Doctor account created.");
      qc.invalidateQueries({ queryKey: queryKeys.admin.doctorProfiles() });
      qc.invalidateQueries({ queryKey: queryKeys.admin.departments() });
      onSuccess?.();
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to create doctor."));
    },
  });
}

export function useAssignDoctorRoom() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: ({ doctorProfileId, roomId }: { doctorProfileId: number; roomId: number | null }) =>
      adminApi.assignDoctorRoom(doctorProfileId, roomId),
    onSuccess: () => {
      toast.success("Room assignment updated.");
      qc.invalidateQueries({ queryKey: queryKeys.admin.doctorProfiles() });
      qc.invalidateQueries({ queryKey: queryKeys.admin.rooms() });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to update room assignment."));
    },
  });
}

export function useSpecializations() {
  return useQuery({
    queryKey: queryKeys.admin.specializations(),
    queryFn: adminApi.getAllSpecializations,
    staleTime: 5 * 60_000, // specializations change infrequently
  });
}
