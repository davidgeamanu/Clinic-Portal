package com.clinic.portal.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Medical notes recorded by the doctor after completing an appointment.
 */
@Entity
@Table(name = "consultation_notes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationNote extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(columnDefinition = "TEXT")
    private String prescription;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "consultationNote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MedicalDocument> documents = new ArrayList<>();
}
