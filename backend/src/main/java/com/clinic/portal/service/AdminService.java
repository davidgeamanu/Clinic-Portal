package com.clinic.portal.service;

import com.clinic.portal.dto.admin.AdminAnalyticsDTO;
import com.clinic.portal.dto.admin.AdminDashboardDTO;
import com.clinic.portal.dto.admin.AdminDepartmentDTO;
import com.clinic.portal.dto.admin.AdminPatientDTO;
import com.clinic.portal.dto.admin.RoomResponseDTO;
import com.clinic.portal.dto.admin.DoctorRoomAssignmentDTO;
import com.clinic.portal.dto.admin.RoomUpdateDTO;
import com.clinic.portal.dto.appointment.AppointmentResponseDTO;
import com.clinic.portal.dto.doctor.AdminCreateDoctorDTO;
import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.specialization.SpecializationResponseDTO;
import com.clinic.portal.dto.user.UserResponseDTO;
import com.clinic.portal.model.enums.AppointmentMode;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {

    AdminDashboardDTO getAdminDashboard();

    AdminAnalyticsDTO getAnalytics();

    List<AdminDepartmentDTO> getDepartments();

    List<RoomResponseDTO> getRooms();

    RoomResponseDTO updateRoom(Long roomId, RoomUpdateDTO dto);

    Page<AdminPatientDTO> getAdminPatients(String search, Boolean active, Pageable pageable);

    List<AppointmentResponseDTO> getPatientAppointments(Long patientProfileId);

    List<AppointmentResponseDTO> getDoctorAppointments(Long doctorProfileId);

    List<UserResponseDTO> getAllUsers();

    List<UserResponseDTO> getUsersByRole(Role role);

    UserResponseDTO setActiveStatus(Long userId, boolean active);

    DoctorProfileResponseDTO createDoctor(AdminCreateDoctorDTO dto);

    DoctorProfileResponseDTO assignDoctorRoom(Long doctorProfileId, DoctorRoomAssignmentDTO dto);

    List<SpecializationResponseDTO> getAllSpecializations();

    Page<AppointmentResponseDTO> getAllAppointments(String search, AppointmentStatus status,
                                                    AppointmentMode mode, Pageable pageable);

    AppointmentResponseDTO cancelAppointment(Long appointmentId);
}