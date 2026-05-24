package com.clinic.notificationservice.service;

import com.clinic.notificationservice.channel.email.EmailChannel;
import com.clinic.notificationservice.channel.inapp.InAppChannel;
import com.clinic.notificationservice.dto.NotificationMapper;
import com.clinic.notificationservice.dto.NotificationResponseDTO;
import com.clinic.notificationservice.model.Notification;
import com.clinic.notificationservice.model.NotificationType;
import com.clinic.notificationservice.repository.NotificationRepository;
import com.clinic.notificationservice.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationMapper notificationMapper;
    @Mock private InAppChannel inAppChannel;
    @Mock private EmailChannel emailChannel;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        var registry = new NotificationMessageStrategyRegistry(List.of(
                new AppointmentScheduledMessage(), new AppointmentConfirmedMessage(),
                new AppointmentStartedMessage(), new AppointmentCancelledMessage(),
                new ConsultationNoteAddedMessage(), new GeneralMessage()
        ));
        notificationService = new NotificationService(
                notificationRepository, notificationMapper, registry, inAppChannel, emailChannel
        );
    }

    @Test
    void dispatch_callsBothChannels() {
        var context = new NotificationContext(LocalDateTime.now(), "John", "Dr Smith");

        notificationService.dispatch(1L, "john@test.com",
                NotificationType.APPOINTMENT_SCHEDULED, context, 100L);

        verify(inAppChannel).send(eq(1L), anyString(),
                eq(NotificationType.APPOINTMENT_SCHEDULED), eq(100L));
        verify(emailChannel).send(eq("john@test.com"), anyString(),
                eq(NotificationType.APPOINTMENT_SCHEDULED));
    }

    @Test
    void dispatch_emailFailureDoesNotBlockInApp() {
        var context = new NotificationContext(LocalDateTime.now(), "John", "Dr Smith");
        doThrow(new RuntimeException("SMTP down")).when(emailChannel)
                .send(anyString(), anyString(), any());

        assertDoesNotThrow(() ->
                notificationService.dispatch(1L, "john@test.com",
                        NotificationType.APPOINTMENT_CONFIRMED, context, 100L));

        verify(inAppChannel).send(eq(1L), anyString(), any(), eq(100L));
    }

    @Test
    void getNotifications_returnsMappedList() {
        Notification n = Notification.builder().userId(1L).message("test")
                .type(NotificationType.GENERAL).build();
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(n));
        when(notificationMapper.toDto(n)).thenReturn(
                new NotificationResponseDTO(1L, "test", NotificationType.GENERAL, false, null, null));

        List<NotificationResponseDTO> result = notificationService.getNotifications(1L);

        assertEquals(1, result.size());
        assertEquals("test", result.getFirst().message());
    }

    @Test
    void markAsRead_setsReadFlag() {
        Notification n = Notification.builder().userId(5L).message("m")
                .type(NotificationType.GENERAL).build();
        n.setId(10L);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));

        notificationService.markAsRead(10L, 5L);

        assertTrue(n.isRead());
        verify(notificationRepository).save(n);
    }

    @Test
    void markAsRead_wrongUser_throws403() {
        Notification n = Notification.builder().userId(5L).message("m")
                .type(NotificationType.GENERAL).build();
        n.setId(10L);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));

        assertThrows(ResponseStatusException.class, () ->
                notificationService.markAsRead(10L, 999L));
    }

    @Test
    void markAsRead_notFound_throws404() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                notificationService.markAsRead(99L, 1L));
    }
}
