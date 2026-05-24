package com.clinic.notificationservice.strategy;

import com.clinic.notificationservice.model.NotificationType;

/**
 * Strategy role in the Strategy pattern.
 */
public interface NotificationMessageStrategy {

    NotificationType type();

    String build(NotificationContext context);
}
