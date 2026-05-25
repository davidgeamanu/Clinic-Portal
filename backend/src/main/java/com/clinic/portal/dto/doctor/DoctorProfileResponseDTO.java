package com.clinic.portal.dto.doctor;

import com.clinic.portal.dto.specialization.SpecializationResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public record DoctorProfileResponseDTO(
        Long id,
        Long userId,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        boolean active,
        String licenseNumber,
        String biography,
        BigDecimal consultationFee,
        BigDecimal rating,
        Long roomId,
        String roomNumber,
        List<SpecializationResponseDTO> specializations,
        Double avgConsultationMinutes,
        Long completedPatientCount
) {}
