package com.clinic.notificationservice.channel.inapp;

import com.clinic.notificationservice.model.Notification;
import com.clinic.notificationservice.model.NotificationType;
import com.clinic.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InAppChannel {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification send(Long userId, String message, NotificationType type, Long relatedEntityId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .message(message)
                .type(type)
                .relatedEntityId(relatedEntityId)
                .build();
        return notificationRepository.save(notification);
    }
}
