package com.clinic.portal.mapper;

import com.clinic.portal.dto.doctor.DoctorProfileResponseDTO;
import com.clinic.portal.dto.specialization.SpecializationResponseDTO;
import com.clinic.portal.model.DoctorProfile;
import com.clinic.portal.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DoctorProfileMapper {

    private final SpecializationMapper specializationMapper;

    public DoctorProfileMapper(SpecializationMapper specializationMapper) {
        this.specializationMapper = specializationMapper;
    }

    public DoctorProfileResponseDTO toDto(DoctorProfile profile) {
        return toDto(profile, null, null);
    }

    public DoctorProfileResponseDTO toDto(DoctorProfile profile, Double avgConsultationMinutes) {
        return toDto(profile, avgConsultationMinutes, null);
    }

    public DoctorProfileResponseDTO toDto(DoctorProfile profile, Double avgConsultationMinutes, Long completedPatientCount) {
        User user = profile.getUser();

        List<SpecializationResponseDTO> specializations = profile.getSpecializations()
                .stream()
                .map(specializationMapper::toDto)
                .toList();

        return new DoctorProfileResponseDTO(
                profile.getId(),
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.isActive(),
                profile.getLicenseNumber(),
                profile.getBiography(),
                profile.getConsultationFee(),
                profile.getRating(),
                profile.getRoom() != null ? profile.getRoom().getId() : null,
                profile.getRoom() != null ? profile.getRoom().getRoomNumber() : null,
                specializations,
                avgConsultationMinutes,
                completedPatientCount
        );
    }
}
