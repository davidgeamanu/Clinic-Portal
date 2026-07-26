package com.clinic.portal.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * A recurring weekly working window for a doctor (e.g. MONDAY 09:00–17:00).
 *
 * A doctor may define several windows per day (e.g. 09:00–12:00 and
 * 14:00–17:00). Booking is only allowed inside a window once the doctor has
 * configured any availability; doctors with no rows accept any future slot.
 */
@Entity
@Table(name = "doctor_availability")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailability extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;
}
