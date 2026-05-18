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
    ACCOUNT_DEACTIVATED("This account has been deactivated. Please contact the clinic.", "ERR_2003"),

    // Patient
    PATIENT_NOT_FOUND("Patient not found.", "ERR_3001"),

    // Doctor
    DOCTOR_NOT_FOUND("Doctor not found.", "ERR_4001"),
    LICENSE_NUMBER_ALREADY_EXISTS("A doctor with this license number already exists.", "ERR_4002"),

    // Specialization
    SPECIALIZATION_NOT_FOUND("Specialization not found.", "ERR_5001"),
    SPECIALIZATION_ALREADY_EXISTS("Specialization already exists.", "ERR_5002"),

    // Appointment
    APPOINTMENT_NOT_FOUND("Appointment not found.", "ERR_6001"),
    APPOINTMENT_SLOT_TAKEN("The selected time slot is already booked.", "ERR_6002"),
    APPOINTMENT_CANNOT_BE_MODIFIED("This appointment cannot be modified in its current state.", "ERR_6003"),
    APPOINTMENT_NOT_YET_STARTABLE("This appointment cannot be started before its scheduled time.", "ERR_6004"),
    APPOINTMENT_NOT_COMPLETED("Only completed appointments can be rated.", "ERR_6005"),
    APPOINTMENT_ALREADY_RATED("This appointment has already been rated.", "ERR_6006"),

    // Consultation & Documents
    CONSULTATION_NOTE_NOT_FOUND("Consultation note not found.", "ERR_7001"),
    MEDICAL_DOCUMENT_NOT_FOUND("Medical document not found.", "ERR_7002"),
    CONSULTATION_NOTE_ALREADY_EXISTS("A consultation note already exists for this appointment.", "ERR_7003"),
    INVALID_FILE_TYPE("File type not allowed. Accepted: PDF, images, Word documents.", "ERR_7004"),
    FILE_STORAGE_ERROR("Failed to store the uploaded file.", "ERR_7005"),

    // Room
    ROOM_NOT_FOUND("Room not found.", "ERR_7006"),
    ROOM_NOT_CONSULT_TYPE("Only consult rooms can be assigned to doctors.", "ERR_7007"),

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