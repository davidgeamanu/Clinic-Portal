package com.clinic.portal.model;

import com.clinic.portal.model.enums.RoomStatus;
import com.clinic.portal.model.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room extends BaseEntity {

    @Column(nullable = false, unique = true, length = 10)
    private String roomNumber;

    @Column(nullable = false)
    private int floor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RoomStatus status = RoomStatus.FREE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;
}