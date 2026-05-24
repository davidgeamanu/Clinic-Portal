package com.clinic.notificationservice.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * In-app notification delivered to a user.
 *
 * Note: {@code userId} is a plain Long, NOT a FK to a User table —
 * the User entity lives in the Clinic Portal database (different bounded
 * context). Cross-database joins are not allowed; the consumer trusts
 * the userId carried in the fat event.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Builder.Default
    @Column(nullable = false)
    private boolean read = false;

    @Column
    private Long relatedEntityId;
}
