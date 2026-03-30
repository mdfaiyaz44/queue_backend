package com.queueless.queueless.controller;

import com.queueless.queueless.common.ApiResponse;
import com.queueless.queueless.dto.notification.NotificationResponse;
import com.queueless.queueless.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Notifications fetched successfully",
                notificationService.getCurrentUserNotifications(principal)
        ));
    }

    @GetMapping(value = "/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMyNotifications(Principal principal) {
        return notificationService.subscribe(principal);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAdminNotifications() {
        return ResponseEntity.ok(ApiResponse.success(
                "Recent notifications fetched successfully",
                notificationService.getRecentNotificationsForAdmin()
        ));
    }
}
