package com.clinic.portal.mapper;

import com.clinic.portal.dto.notification.NotificationResponseDTO;
import com.clinic.portal.model.Notification;
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
