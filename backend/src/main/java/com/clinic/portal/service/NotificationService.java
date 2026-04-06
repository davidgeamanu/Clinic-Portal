package com.clinic.portal.service;

import com.clinic.portal.dto.notification.NotificationResponseDTO;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    List<NotificationResponseDTO> getNotifications(Long userId);

    List<NotificationResponseDTO> getUnreadNotifications(Long userId);

    long countUnread(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    // called by other services to dispatch a notification. Not an endpoint
    void send(User user, String message, NotificationType type, Long relatedEntityId);
}
