package com.clinic.portal.service.impl;

import com.clinic.portal.dto.notification.NotificationResponseDTO;
import com.clinic.portal.exception.DataNotFoundException;
import com.clinic.portal.exception.ExceptionCode;
import com.clinic.portal.exception.UnauthorizedException;
import com.clinic.portal.mapper.NotificationMapper;
import com.clinic.portal.model.Notification;
import com.clinic.portal.model.User;
import com.clinic.portal.model.enums.NotificationType;
import com.clinic.portal.repository.NotificationRepository;
import com.clinic.portal.repository.UserRepository;
import com.clinic.portal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public List<NotificationResponseDTO> getNotifications(Long userId) {
        User user = findUser(userId);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(notificationMapper::toDto).toList();
    }

    @Override
    public List<NotificationResponseDTO> getUnreadNotifications(Long userId) {
        User user = findUser(userId);
        return notificationRepository.findByUserAndReadFalse(user)
                .stream().map(notificationMapper::toDto).toList();
    }

    @Override
    public long countUnread(Long userId) {
        User user = findUser(userId);
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.NOTIFICATION_NOT_FOUND));

        // Ensure users can only mark their own notifications as read
        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException(ExceptionCode.ACCESS_DENIED);
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        User user = findUser(userId);
        List<Notification> unread = notificationRepository.findByUserAndReadFalse(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public void send(User user, String message, NotificationType type, Long relatedEntityId) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .type(type)
                .relatedEntityId(relatedEntityId)
                .build();

        notificationRepository.save(notification);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND));
    }
}
