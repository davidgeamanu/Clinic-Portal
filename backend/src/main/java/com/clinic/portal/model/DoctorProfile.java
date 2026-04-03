package com.clinic.portal.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctor_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 100)
    private String licenseNumber;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(precision = 10, scale = 2)
    private BigDecimal consultationFee;

    /**
     * Doctor <-> Specialization: many-to-many.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "doctor_specializations",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    @Builder.Default
    private List<Specialization> specializations = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();
}
