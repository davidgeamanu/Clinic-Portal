package com.clinic.notificationservice.repository;

import com.clinic.notificationservice.model.Notification;
import com.clinic.notificationservice.model.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryIntegrationTest {

    @Autowired private NotificationRepository notificationRepository;

    @Test
    void findByUserIdOrderByCreatedAtDesc_returnsUserNotificationsOnly() {
        notificationRepository.save(notification(1L, "msg1", false));
        notificationRepository.save(notification(1L, "msg2", true));
        notificationRepository.save(notification(2L, "msg3", false));

        List<Notification> result = notificationRepository.findByUserIdOrderByCreatedAtDesc(1L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(n -> n.getUserId().equals(1L)));
    }

    @Test
    void findByUserIdAndReadFalse_returnsOnlyUnread() {
        notificationRepository.save(notification(1L, "unread", false));
        notificationRepository.save(notification(1L, "read", true));

        List<Notification> result = notificationRepository.findByUserIdAndReadFalse(1L);

        assertEquals(1, result.size());
        assertFalse(result.getFirst().isRead());
    }

    @Test
    void countByUserIdAndReadFalse_countsCorrectly() {
        notificationRepository.save(notification(1L, "a", false));
        notificationRepository.save(notification(1L, "b", false));
        notificationRepository.save(notification(1L, "c", true));

        long count = notificationRepository.countByUserIdAndReadFalse(1L);

        assertEquals(2, count);
    }

    @Test
    void countByUserIdAndReadFalse_returnsZeroWhenAllRead() {
        notificationRepository.save(notification(1L, "a", true));

        long count = notificationRepository.countByUserIdAndReadFalse(1L);

        assertEquals(0, count);
    }

    private Notification notification(Long userId, String message, boolean read) {
        return Notification.builder()
                .userId(userId)
                .message(message)
                .type(NotificationType.GENERAL)
                .read(read)
                .build();
    }
}
