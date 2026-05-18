package com.clinic.portal.mapper;

import com.clinic.portal.dto.patient.PatientProfileResponseDTO;
import com.clinic.portal.model.PatientProfile;
import com.clinic.portal.model.User;
import org.springframework.stereotype.Component;

@Component
public class PatientProfileMapper {

    public PatientProfileResponseDTO toDto(PatientProfile profile) {
        User user = profile.getUser();
        return new PatientProfileResponseDTO(
                profile.getId(),
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                profile.getDateOfBirth(),
                profile.getGender(),
                profile.getBloodType(),
                profile.getAddress(),
                profile.getEmergencyContactName(),
                profile.getEmergencyContactPhone(),
                profile.getHeightCm(),
                profile.getWeightKg(),
                profile.getAllergies(),
                profile.getChronicConditions(),
                profile.getFamilyHistory(),
                profile.getLifestyleSmoking(),
                profile.getLifestyleAlcohol(),
                profile.getLifestyleExercise(),
                profile.getLifestyleDiet()
        );
    }
}
