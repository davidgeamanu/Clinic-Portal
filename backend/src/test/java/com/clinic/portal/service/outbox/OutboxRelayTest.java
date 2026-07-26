package com.clinic.portal.service.outbox;

import com.clinic.portal.model.OutboxEvent;
import com.clinic.portal.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock private OutboxEventRepository outboxEventRepository;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OutboxRelay relay;

    private OutboxEvent pendingEvent(long id) {
        OutboxEvent event = OutboxEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("AppointmentBookedEvent")
                .queue("test.booked")
                .payload("{\"appointmentId\":" + id + "}")
                .correlationId("corr-" + id)
                .build();
        event.setId(id);
        return event;
    }

    @Test
    void publishPending_sendsJsonMessageAndMarksPublished() {
        OutboxEvent event = pendingEvent(1L);
        when(outboxEventRepository.findTop50ByPublishedAtIsNullOrderByIdAsc())
                .thenReturn(List.of(event));

        relay.publishPending();

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(rabbitTemplate).send(eq("test.booked"), captor.capture());

        Message message = captor.getValue();
        assertEquals("{\"appointmentId\":1}", new String(message.getBody()));
        assertEquals("application/json", message.getMessageProperties().getContentType());
        assertEquals(event.getEventId().toString(), message.getMessageProperties().getMessageId());
        assertEquals("corr-1", message.getMessageProperties().getHeader(OutboxRelay.CORRELATION_ID_HEADER));

        assertNotNull(event.getPublishedAt());
        verify(outboxEventRepository).save(event);
    }

    @Test
    void publishPending_brokerDown_leavesRowsUnpublishedForRetry() {
        OutboxEvent first = pendingEvent(1L);
        OutboxEvent second = pendingEvent(2L);
        when(outboxEventRepository.findTop50ByPublishedAtIsNullOrderByIdAsc())
                .thenReturn(List.of(first, second));
        doThrow(new AmqpConnectException(new RuntimeException("broker down")))
                .when(rabbitTemplate).send(any(), any(Message.class));

        relay.publishPending();

        assertNull(first.getPublishedAt());
        assertNull(second.getPublishedAt());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void publishPending_noPendingRows_doesNothing() {
        when(outboxEventRepository.findTop50ByPublishedAtIsNullOrderByIdAsc())
                .thenReturn(List.of());

        relay.publishPending();

        verifyNoInteractions(rabbitTemplate);
    }
}
