package com.clinic.notificationservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Idempotency ledger: one row per consumed RabbitMQ event.
 *
 * The outbox relay on the portal side delivers at-least-once, so redelivered
 * events are possible (broker restart, consumer crash between dispatch and
 * ack). The unique {@code eventId} lets the consumer skip duplicates instead
 * of sending the same notification twice.
 */
@Entity
@Table(name = "processed_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent extends BaseEntity {

    @Column(nullable = false, unique = true)
    private UUID eventId;

    @Column(nullable = false, length = 100)
    private String eventType;
}
