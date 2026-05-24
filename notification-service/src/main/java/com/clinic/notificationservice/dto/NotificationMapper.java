package com.clinic.notificationservice.dto;

import com.clinic.notificationservice.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponseDTO toDto(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getRelatedEntityId(),
                notification.getCreatedAt()
        );
    }
}
