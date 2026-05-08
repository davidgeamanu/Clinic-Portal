package com.clinic.portal.dto.doctor;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record DoctorProfileUpdateDTO(

        @Size(max = 1000)
        String biography,

        @DecimalMin(value = "0.0", message = "Consultation fee cannot be negative")
        BigDecimal consultationFee,

        @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
        @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
        BigDecimal rating,

        List<Long> specializationIds

) {}
