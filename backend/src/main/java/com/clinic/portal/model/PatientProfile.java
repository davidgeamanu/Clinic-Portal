package com.clinic.portal.model;

import com.clinic.portal.model.enums.BloodType;
import com.clinic.portal.model.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Patient-specific data, extends the base User identity.
 */
@Entity
@Table(name = "patient_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BloodType bloodType;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 150)
    private String emergencyContactName;

    @Column(length = 20)
    private String emergencyContactPhone;

    @OneToMany(mappedBy = "patient", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();
}
