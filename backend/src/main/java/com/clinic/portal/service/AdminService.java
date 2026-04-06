package com.clinic.portal.service;

import com.clinic.portal.dto.doctor.AdminCreateDoctorDTO;
import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.specialization.SpecializationRequestDTO;
import com.clinic.portal.dto.specialization.SpecializationResponseDTO;
import com.clinic.portal.dto.user.UserResponseDTO;
import com.clinic.portal.model.enums.Role;

import java.util.List;

public interface AdminService {

    List<UserResponseDTO> getAllUsers();

    List<UserResponseDTO> getUsersByRole(Role role);

    UserResponseDTO setActiveStatus(Long userId, boolean active);

    DoctorProfileResponseDTO createDoctor(AdminCreateDoctorDTO dto);

    List<SpecializationResponseDTO> getAllSpecializations();

    SpecializationResponseDTO createSpecialization(SpecializationRequestDTO dto);

    void deleteSpecialization(Long id);
}
