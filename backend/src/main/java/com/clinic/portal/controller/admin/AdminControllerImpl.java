package com.clinic.portal.controller.admin;

import com.clinic.portal.dto.admin.AdminAnalyticsDTO;
import com.clinic.portal.dto.admin.AdminDashboardDTO;
import com.clinic.portal.dto.admin.AdminDepartmentDTO;
import com.clinic.portal.dto.admin.AdminPatientDTO;
import com.clinic.portal.dto.admin.DoctorRoomAssignmentDTO;
import com.clinic.portal.dto.admin.RoomResponseDTO;
import com.clinic.portal.dto.admin.RoomUpdateDTO;
import com.clinic.portal.dto.appointment.AppointmentResponseDTO;
import com.clinic.portal.dto.doctor.AdminCreateDoctorDTO;
import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.specialization.SpecializationResponseDTO;
import com.clinic.portal.dto.user.UserResponseDTO;
import com.clinic.portal.model.enums.AppointmentMode;
import com.clinic.portal.model.enums.AppointmentStatus;
import com.clinic.portal.model.enums.Role;
import com.clinic.portal.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminControllerImpl implements AdminController {

    private final AdminService adminService;

    @Override
    public AdminDashboardDTO getAdminDashboard() {
        log.info("[ADMIN] Getting dashboard stats");
        return adminService.getAdminDashboard();
    }

    @Override
    public AdminAnalyticsDTO getAnalytics() {
        log.info("[ADMIN] Getting analytics data");
        return adminService.getAnalytics();
    }

    @Override
    public List<AdminDepartmentDTO> getDepartments() {
        log.info("[ADMIN] Getting departments");
        return adminService.getDepartments();
    }

    @Override
    public List<RoomResponseDTO> getRooms() {
        log.info("[ADMIN] Getting all rooms");
        return adminService.getRooms();
    }

    @Override
    public RoomResponseDTO updateRoom(Long roomId, RoomUpdateDTO dto) {
        log.info("[ADMIN] Updating room: {}", roomId);
        return adminService.updateRoom(roomId, dto);
    }

    @Override
    public PagedModel<AdminPatientDTO> getAdminPatients(String search, Boolean active, Pageable pageable) {
        log.info("[ADMIN] Getting patients page {} (size {}, search '{}', active {})",
                pageable.getPageNumber(), pageable.getPageSize(), search, active);
        return new PagedModel<>(adminService.getAdminPatients(search, active, pageable));
    }

    @Override
    public List<AppointmentResponseDTO> getPatientAppointments(Long patientProfileId) {
        log.info("[ADMIN] Getting appointments for patient profile: {}", patientProfileId);
        return adminService.getPatientAppointments(patientProfileId);
    }

    @Override
    public List<AppointmentResponseDTO> getDoctorAppointments(Long doctorProfileId) {
        log.info("[ADMIN] Getting appointments for doctor profile: {}", doctorProfileId);
        return adminService.getDoctorAppointments(doctorProfileId);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        log.info("[ADMIN] Getting all users");
        return adminService.getAllUsers();
    }

    @Override
    public List<UserResponseDTO> getUsersByRole(Role role) {
        log.info("[ADMIN] Getting users by role: {}", role);
        return adminService.getUsersByRole(role);
    }

    @Override
    public UserResponseDTO setActiveStatus(Long userId, boolean active) {
        log.info("[ADMIN] Setting active={} for user: {}", active, userId);
        return adminService.setActiveStatus(userId, active);
    }

    @Override
    public DoctorProfileResponseDTO assignDoctorRoom(Long doctorProfileId, DoctorRoomAssignmentDTO dto) {
        log.info("[ADMIN] Assigning room {} to doctor profile: {}", dto.roomId(), doctorProfileId);
        return adminService.assignDoctorRoom(doctorProfileId, dto);
    }

    @Override
    public DoctorProfileResponseDTO createDoctor(AdminCreateDoctorDTO dto) {
        log.info("[ADMIN] Creating doctor: {}", dto.email());
        return adminService.createDoctor(dto);
    }

    @Override
    public List<SpecializationResponseDTO> getAllSpecializations() {
        log.info("[ADMIN] Getting all specializations");
        return adminService.getAllSpecializations();
    }

    @Override
    public PagedModel<AppointmentResponseDTO> getAllAppointments(String search, AppointmentStatus status,
                                                                 AppointmentMode mode, Pageable pageable) {
        log.info("[ADMIN] Getting appointments page {} (size {}, search '{}', status {}, mode {})",
                pageable.getPageNumber(), pageable.getPageSize(), search, status, mode);
        return new PagedModel<>(adminService.getAllAppointments(search, status, mode, pageable));
    }

    @Override
    public AppointmentResponseDTO cancelAppointment(Long appointmentId) {
        log.info("[ADMIN] Cancelling appointment: {}", appointmentId);
        return adminService.cancelAppointment(appointmentId);
    }
}