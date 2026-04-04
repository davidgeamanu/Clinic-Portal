package com.clinic.portal.dto.doctor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;


public record DoctorProfileUpdateDTO(

        @Size(max = 1000)
        String biography,

        @DecimalMin(value = "0.0", message = "Consultation fee cannot be negative")
        BigDecimal consultationFee,

        List<Long> specializationIds

) {}
