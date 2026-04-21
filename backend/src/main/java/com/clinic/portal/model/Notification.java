package com.clinic.portal.model;

import com.clinic.portal.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

/**
 * An in-app notification delivered to a user.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
