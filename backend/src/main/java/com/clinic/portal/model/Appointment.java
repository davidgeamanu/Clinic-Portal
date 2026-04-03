package com.clinic.portal.model;

import com.clinic.portal.model.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * An appointment booking between a patient and a doctor.
 *
 * This is the central entity of the system — it links PatientProfile,
 * DoctorProfile, and (after completion) ConsultationNote.
 *
 * Both @ManyToOne relationships use FetchType.LAZY (default for @ManyToOne
 * is actually EAGER — we override it to LAZY here because loading every
 * appointment should not auto-join the full patient and doctor rows).
 */
@Entity
@Table(name = "appointments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ConsultationNote consultationNote;
}
