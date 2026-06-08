package com.clinic.notificationservice.controller;

import com.clinic.notificationservice.dto.NotificationResponseDTO;
import com.clinic.notificationservice.security.UserDetailsImpl;
import com.clinic.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notification management")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my notifications", description = "Returns all notifications for the currently authenticated user, newest first.")
    @ApiResponse(responseCode = "200", description = "Notifications retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = NotificationResponseDTO.class)))
    public List<NotificationResponseDTO> getAll(@AuthenticationPrincipal UserDetailsImpl user) {
        return notificationService.getNotifications(user.getId());
    }

    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark notification as read", description = "Marks a single notification as read. Only the owner of the notification can do this.")
    @ApiResponse(responseCode = "204", description = "Marked as read")
    public void markAsRead(@PathVariable Long notificationId,
                           @AuthenticationPrincipal UserDetailsImpl user) {
        notificationService.markAsRead(notificationId, user.getId());
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark all notifications as read", description = "Marks every unread notification for the current user as read in one go.")
    @ApiResponse(responseCode = "204", description = "All marked as read")
    public void markAllAsRead(@AuthenticationPrincipal UserDetailsImpl user) {
        notificationService.markAllAsRead(user.getId());
    }
}
