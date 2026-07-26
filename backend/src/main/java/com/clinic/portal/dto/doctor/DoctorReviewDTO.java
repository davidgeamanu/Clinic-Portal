package com.clinic.portal.dto.doctor;

/**
 * A single patient review of a completed appointment, shown on the doctor's
 * public profile. The patient is identified by first name and last initial
 * only (e.g. "John D.").
 */
public record DoctorReviewDTO(
        int rating,
        String review,
        String patientName,
        String visitDate
) {}
