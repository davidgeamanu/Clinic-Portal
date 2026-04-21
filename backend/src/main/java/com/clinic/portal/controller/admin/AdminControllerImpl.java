package com.clinic.portal.controller.admin;

import com.clinic.portal.dto.doctor.AdminCreateDoctorDTO;
import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.doctor.DoctorProfileUpdateDTO;
import com.clinic.portal.dto.specialization.SpecializationRequestDTO;
import com.clinic.portal.dto.specialization.SpecializationResponseDTO;
import com.clinic.portal.dto.user.UserResponseDTO;
import com.clinic.portal.model.enums.Role;
import com.clinic.portal.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public DoctorProfileResponseDTO createDoctor(AdminCreateDoctorDTO dto) {
        log.info("[ADMIN] Creating doctor: {}", dto.email());
        return adminService.createDoctor(dto);
    }

    @Override
    public DoctorProfileResponseDTO updateDoctor(Long profileId, DoctorProfileUpdateDTO dto) {
        log.info("[ADMIN] Updating doctor profile: {}", profileId);
        return adminService.updateDoctor(profileId, dto);
    }

    @Override
    public List<SpecializationResponseDTO> getAllSpecializations() {
        log.info("[ADMIN] Getting all specializations");
        return adminService.getAllSpecializations();
    }

    @Override
    public SpecializationResponseDTO createSpecialization(SpecializationRequestDTO dto) {
        log.info("[ADMIN] Creating specialization: {}", dto.name());
        return adminService.createSpecialization(dto);
    }

    @Override
    public void deleteSpecialization(Long id) {
        log.info("[ADMIN] Deleting specialization: {}", id);
        adminService.deleteSpecialization(id);
    }
}