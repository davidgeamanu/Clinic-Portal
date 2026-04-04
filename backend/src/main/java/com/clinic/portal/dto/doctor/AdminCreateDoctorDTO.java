package com.clinic.portal.dto.doctor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminCreateDoctorDTO(

        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8)
        String password,

        @NotBlank @Size(max = 100)
        String firstName,

        @NotBlank @Size(max = 100)
        String lastName,

        @Size(max = 20)
        String phoneNumber,

        @NotBlank @Size(max = 100)
        String licenseNumber,

        @Size(max = 1000)
        String biography,

        List<Long> specializationIds

) {}
