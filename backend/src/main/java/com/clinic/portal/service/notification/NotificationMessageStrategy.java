package com.clinic.portal.service.notification;

import com.clinic.portal.model.Appointment;
import com.clinic.portal.model.enums.NotificationType;

/**
 * Strategy role in the Strategy pattern.
 *
 * Each implementation encapsulates the message-text template for one
 * {@link NotificationType}.
 */
public interface NotificationMessageStrategy {

    NotificationType type();

    String build(Appointment appointment);
}