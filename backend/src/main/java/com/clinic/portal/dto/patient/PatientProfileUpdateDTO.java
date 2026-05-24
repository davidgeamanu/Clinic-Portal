package com.clinic.portal.dto.patient;

import com.clinic.portal.model.enums.BloodType;
import com.clinic.portal.model.enums.Gender;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;


public record PatientProfileUpdateDTO(

        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Size(max = 20)
        String phoneNumber,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        Gender gender,
        BloodType bloodType,

        @Size(max = 500)
        String address,

        @Size(max = 150)
        String emergencyContactName,

        @Size(max = 20)
        String emergencyContactPhone,

        @Min(value = 30,  message = "Height must be at least 30 cm")
        @Max(value = 250, message = "Height must be at most 250 cm")
        Integer heightCm,

        @DecimalMin(value = "1.0",   message = "Weight must be at least 1 kg")
        @DecimalMax(value = "500.0", message = "Weight must be at most 500 kg")
        BigDecimal weightKg,

        @Size(max = 2000)
        String allergies,

        @Size(max = 2000)
        String chronicConditions,

        @Size(max = 2000)
        String familyHistory,

        @Size(max = 40)
        String lifestyleSmoking,

        @Size(max = 40)
        String lifestyleAlcohol,

        @Size(max = 40)
        String lifestyleExercise,

        @Size(max = 40)
        String lifestyleDiet

) {}
