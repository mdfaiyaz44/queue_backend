package com.queueless.queueless.dto.notification;

import com.queueless.queueless.entity.NotificationChannel;
import com.queueless.queueless.entity.NotificationStatus;
import com.queueless.queueless.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        NotificationChannel channel,
        NotificationStatus status,
        String title,
        String message,
        String recipient,
        LocalDateTime createdAt
) {
}
