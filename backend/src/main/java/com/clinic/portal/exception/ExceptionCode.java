package com.clinic.portal.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionCode {

    // Validation
    VALIDATION_ERROR("Validation failed.", "ERR_1001"),

    // User
    USER_NOT_FOUND("User not found.", "ERR_2001"),
    EMAIL_ALREADY_EXISTS("Email is already registered.", "ERR_2002"),

    // Patient
    PATIENT_NOT_FOUND("Patient not found.", "ERR_3001"),

    // Doctor
    DOCTOR_NOT_FOUND("Doctor not found.", "ERR_4001"),

    // Specialization
    SPECIALIZATION_NOT_FOUND("Specialization not found.", "ERR_5001"),
    SPECIALIZATION_ALREADY_EXISTS("Specialization already exists.", "ERR_5002"),

    // Appointment
    APPOINTMENT_NOT_FOUND("Appointment not found.", "ERR_6001"),
    APPOINTMENT_SLOT_TAKEN("The selected time slot is already booked.", "ERR_6002"),
    APPOINTMENT_CANNOT_BE_MODIFIED("This appointment cannot be modified in its current state.", "ERR_6003"),

    // Consultation & Documents
    CONSULTATION_NOTE_NOT_FOUND("Consultation note not found.", "ERR_7001"),
    MEDICAL_DOCUMENT_NOT_FOUND("Medical document not found.", "ERR_7002"),

    // Notification
    NOTIFICATION_NOT_FOUND("Notification not found.", "ERR_8001"),

    // Authorization
    ACCESS_DENIED("You are not allowed to perform this action.", "ERR_9001"),
    INVALID_CREDENTIALS("Invalid email or password.", "ERR_9002"),

    // Server
    SERVER_ERROR("Internal server error.", "ERR_9999");

    private final String message;
    private final String code;
}