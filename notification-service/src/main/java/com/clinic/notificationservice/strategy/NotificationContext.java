package com.clinic.notificationservice.strategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record NotificationContext(
        LocalDateTime scheduledAt,
        String patientName,
        String doctorName
) {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm");

    public String formattedDate() {
        return scheduledAt.format(FORMATTER);
    }
}
