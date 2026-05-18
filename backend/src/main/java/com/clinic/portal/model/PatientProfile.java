package com.clinic.portal.model;

import com.clinic.portal.model.enums.BloodType;
import com.clinic.portal.model.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Patient-specific data — extends the base User identity.
 *
 * Kept separate from User so that auth concerns and medical profile concerns
 * don't bleed into each other (SRP). If patient fields change, User is untouched.
 *
 * Relationship to User:
 *   @OneToOne  — one PatientProfile belongs to exactly one User.
 *   @JoinColumn(name = "user_id") — creates a "user_id" FK column in this table.
 *   The profile table owns the relationship (it holds the FK), which is correct
 *   because profiles are the "child" — they depend on User, not the other way around.
 *
 *   fetch = FetchType.LAZY — User is not loaded until explicitly accessed.
 *   Always prefer LAZY for @OneToOne and @ManyToOne to avoid unnecessary joins.
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

    private Integer heightCm;

    @Column(precision = 5, scale = 2)
    private BigDecimal weightKg;

    // Free-form, newline-separated lists. Edited and rendered as a whole by the patient —
    // never queried piece-wise, so a join table would only add cost.
    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(columnDefinition = "TEXT")
    private String chronicConditions;

    @Column(columnDefinition = "TEXT")
    private String familyHistory;

    @Column(length = 40)
    private String lifestyleSmoking;

    @Column(length = 40)
    private String lifestyleAlcohol;

    @Column(length = 40)
    private String lifestyleExercise;

    @Column(length = 40)
    private String lifestyleDiet;

    @OneToMany(mappedBy = "patient", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();
}
