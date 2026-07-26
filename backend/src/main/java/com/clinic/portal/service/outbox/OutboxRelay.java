package com.clinic.portal.service.outbox;

import com.clinic.portal.model.OutboxEvent;
import com.clinic.portal.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Publishes pending outbox rows to RabbitMQ (Outbox pattern, relay half).
 *
 * Runs on a short fixed delay. Rows are published oldest-first; on the first
 * broker failure the batch stops so ordering is preserved and the remaining
 * rows are retried next run. Delivery is therefore at-least-once — the
 * consumer deduplicates by {@code eventId}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    public static final String CORRELATION_ID_HEADER = "x-correlation-id";

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelayString = "${app.messaging.outbox-relay-delay-ms:3000}")
    public void publishPending() {
        List<OutboxEvent> pending = outboxEventRepository.findTop50ByPublishedAtIsNullOrderByIdAsc();
        if (pending.isEmpty()) return;

        for (OutboxEvent event : pending) {
            try {
                rabbitTemplate.send(event.getQueue(), toMessage(event));
            } catch (Exception e) {
                log.warn("Outbox publish failed for event {} ({}); will retry: {}",
                        event.getEventId(), event.getEventType(), e.getMessage());
                return;
            }
            event.setPublishedAt(LocalDateTime.now());
            outboxEventRepository.save(event);
            log.info("Published outbox event {} ({}) to queue {}",
                    event.getEventId(), event.getEventType(), event.getQueue());
        }
    }

    private Message toMessage(OutboxEvent event) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setContentEncoding(StandardCharsets.UTF_8.name());
        props.setMessageId(event.getEventId().toString());
        if (event.getCorrelationId() != null) {
            props.setHeader(CORRELATION_ID_HEADER, event.getCorrelationId());
        }
        return new Message(event.getPayload().getBytes(StandardCharsets.UTF_8), props);
    }
}
