package com.clinic.portal.mapper;

import com.clinic.portal.dto.specialization.SpecializationResponseDTO;
import com.clinic.portal.model.Specialization;
import org.springframework.stereotype.Component;

@Component
public class SpecializationMapper {

    public SpecializationResponseDTO toDto(Specialization specialization) {
        return new SpecializationResponseDTO(
                specialization.getId(),
                specialization.getName(),
                specialization.getDescription()
        );
    }
}
