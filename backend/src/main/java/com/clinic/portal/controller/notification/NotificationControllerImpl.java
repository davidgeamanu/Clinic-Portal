package com.clinic.portal.controller.notification;

import com.clinic.portal.dto.notification.NotificationResponseDTO;
import com.clinic.portal.security.UserDetailsImpl;
import com.clinic.portal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationControllerImpl implements NotificationController {

    private final NotificationService notificationService;

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<NotificationResponseDTO> getAll() {
        log.info("[NOTIFICATION] Getting all notifications for user: {}", currentUser().getId());
        return notificationService.getNotifications(currentUser().getId());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<NotificationResponseDTO> getUnread() {
        log.info("[NOTIFICATION] Getting unread notifications for user: {}", currentUser().getId());
        return notificationService.getUnreadNotifications(currentUser().getId());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public long countUnread() {
        return notificationService.countUnread(currentUser().getId());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void markAsRead(Long notificationId) {
        log.info("[NOTIFICATION] Marking notification {} as read for user: {}", notificationId, currentUser().getId());
        notificationService.markAsRead(notificationId, currentUser().getId());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void markAllAsRead() {
        log.info("[NOTIFICATION] Marking all notifications as read for user: {}", currentUser().getId());
        notificationService.markAllAsRead(currentUser().getId());
    }

    private UserDetailsImpl currentUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}