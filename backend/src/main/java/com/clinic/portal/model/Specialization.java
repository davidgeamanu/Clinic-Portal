package com.clinic.portal.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A medical specialization (e.g. "Cardiology", "Dermatology").
 * Managed by Admin. Shared across many doctors.
 *
 * The "inverse" side of the Doctor ↔ Specialization many-to-many.
 * mappedBy means DoctorProfile owns the join table — Specialization just
 * references it without maintaining it.
 */
@Entity
@Table(name = "specializations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Specialization extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "specializations")
    @Builder.Default
    private List<DoctorProfile> doctors = new ArrayList<>();
}
