package com.clinic.portal.controller.notification;

import com.clinic.portal.dto.notification.NotificationResponseDTO;
import com.clinic.portal.exception.ExceptionBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Tag(name = "Notifications", description = "In-app notification management")
public interface NotificationController {

    @GetMapping
    @Operation(summary = "Get all notifications", description = "Retrieve all notifications for the current user, newest first.")
    @ApiResponse(responseCode = "200", description = "Notifications retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = NotificationResponseDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<NotificationResponseDTO> getAll();

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    @ApiResponse(responseCode = "200", description = "Unread notifications retrieved",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = NotificationResponseDTO.class)))
    @ResponseStatus(HttpStatus.OK)
    List<NotificationResponseDTO> getUnread();

    @GetMapping("/unread/count")
    @Operation(summary = "Count unread notifications", description = "Returns the number of unread notifications for the badge indicator.")
    @ApiResponse(responseCode = "200", description = "Count returned")
    @ResponseStatus(HttpStatus.OK)
    long countUnread();

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Marked as read"),
            @ApiResponse(responseCode = "403", description = "Not your notification",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void markAsRead(@PathVariable Long notificationId);

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    @ApiResponse(responseCode = "204", description = "All marked as read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void markAllAsRead();
}