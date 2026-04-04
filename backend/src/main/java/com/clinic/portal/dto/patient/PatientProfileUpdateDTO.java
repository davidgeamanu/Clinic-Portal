package com.clinic.portal.dto.patient;

import com.clinic.portal.model.enums.BloodType;
import com.clinic.portal.model.enums.Gender;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;


public record PatientProfileUpdateDTO(

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        Gender gender,
        BloodType bloodType,

        @Size(max = 500)
        String address,

        @Size(max = 150)
        String emergencyContactName,

        @Size(max = 20)
        String emergencyContactPhone

) {}
