package com.clinic.notificationservice.dto;

import com.clinic.notificationservice.model.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
        Long id,
        String message,
        NotificationType type,
        boolean read,
        Long relatedEntityId,
        LocalDateTime createdAt
) {}
