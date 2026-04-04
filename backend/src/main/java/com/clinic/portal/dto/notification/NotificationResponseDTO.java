package com.clinic.portal.dto.notification;

import com.clinic.portal.model.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
        Long id,
        String message,
        NotificationType type,
        boolean read,
        Long relatedEntityId,
        LocalDateTime createdAt
) {}
