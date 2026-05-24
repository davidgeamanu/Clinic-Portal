package com.clinic.notificationservice.controller;

import com.clinic.notificationservice.dto.NotificationResponseDTO;
import com.clinic.notificationservice.security.UserDetailsImpl;
import com.clinic.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public List<NotificationResponseDTO> getAll(@AuthenticationPrincipal UserDetailsImpl user) {
        return notificationService.getNotifications(user.getId());
    }

    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@PathVariable Long notificationId,
                           @AuthenticationPrincipal UserDetailsImpl user) {
        notificationService.markAsRead(notificationId, user.getId());
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(@AuthenticationPrincipal UserDetailsImpl user) {
        notificationService.markAllAsRead(user.getId());
    }
}
