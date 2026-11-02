package com.rucart.backend.controller;

import com.rucart.backend.dto.ApiResponse;
import com.rucart.backend.dto.NotificationDto;
import com.rucart.backend.model.Notification;
import com.rucart.backend.security.CustomUserDetails;
import com.rucart.backend.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly) {
        if (page != null) {
            Page<Notification> paged = notificationService.getUserNotificationsPaginated(userDetails.getId(), page, size, unreadOnly);
            return ResponseEntity.ok(ApiResponse.ok(paged));
        } else {
            List<Notification> notifications = notificationService.getUserNotifications(userDetails.getId());
            return ResponseEntity.ok(ApiResponse.ok(notifications));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        long count = notificationService.getUnreadCount(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        Notification notification = notificationService.markAsRead(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", notification));
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        int updated = notificationService.markAllAsRead(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", Map.of("updatedCount", updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        notificationService.deleteNotification(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notification deleted successfully", null));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Notification>> sendNotification(
            @Valid @RequestBody NotificationDto dto) {
        Notification notification = notificationService.sendNotification(dto);
        return ResponseEntity.ok(ApiResponse.ok("Notification sent successfully", notification));
    }
}
