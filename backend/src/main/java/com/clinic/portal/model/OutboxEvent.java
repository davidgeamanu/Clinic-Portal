package com.clinic.portal.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transactional outbox row (Outbox pattern).
 *
 * Written in the same transaction as the domain change it describes, so a
 * booking and its event are committed atomically. The {@code OutboxRelay}
 * publishes unpublished rows to RabbitMQ afterwards — if the broker is down,
 * rows simply accumulate and are delivered once it recovers.
 */
@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent extends BaseEntity {

    @Column(nullable = false, unique = true)
    private UUID eventId;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, length = 200)
    private String queue;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(length = 64)
    private String correlationId;

    private LocalDateTime publishedAt;
}
