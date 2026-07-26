package com.clinic.notificationservice.service;

import com.clinic.notificationservice.channel.email.EmailChannel;
import com.clinic.notificationservice.channel.inapp.InAppChannel;
import com.clinic.notificationservice.dto.NotificationMapper;
import com.clinic.notificationservice.dto.NotificationResponseDTO;
import com.clinic.notificationservice.model.Notification;
import com.clinic.notificationservice.model.NotificationType;
import com.clinic.notificationservice.repository.NotificationRepository;
import com.clinic.notificationservice.sse.SseEmitterRegistry;
import com.clinic.notificationservice.strategy.NotificationContext;
import com.clinic.notificationservice.strategy.NotificationMessageStrategyRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationMessageStrategyRegistry strategyRegistry;
    private final InAppChannel inAppChannel;
    private final EmailChannel emailChannel;
    private final SseEmitterRegistry sseEmitterRegistry;

    /**
     * Builds the message from the strategy, fans out to in-app + email + SSE
     * channels. Called by the RabbitMQ consumer.
     *
     * The notification row is written inside the transaction; the SSE push and
     * the email are deferred until after it commits. Both are irreversible once
     * they leave the process, so firing them early would mean a rolled-back
     * transaction could still produce a toast and an email for a notification
     * that does not exist — and the SSE push would race the browser's refetch,
     * which cannot see uncommitted rows.
     */
    @Transactional
    public void dispatch(Long userId, String recipientEmail, String recipientName,
                         NotificationType type, NotificationContext context, Long relatedEntityId) {
        String message = strategyRegistry.forType(type).build(context);

        Notification saved = inAppChannel.send(userId, message, type, relatedEntityId);
        NotificationResponseDTO dto = notificationMapper.toDto(saved);

        afterCommit(() -> {
            // Push to any open browser connections so the UI updates without polling
            sseEmitterRegistry.push(userId, dto);
            try {
                emailChannel.send(recipientEmail, recipientName, message, type);
            } catch (Exception e) {
                log.error("Email dispatch failed for user {} / type {}", userId, type, e);
            }
        });
    }

    /**
     * Runs {@code action} once the current transaction commits, or immediately
     * when there is no transaction in progress (unit tests calling dispatch
     * directly).
     */
    private void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    public List<NotificationResponseDTO> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(notificationMapper::toDto).toList();
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
