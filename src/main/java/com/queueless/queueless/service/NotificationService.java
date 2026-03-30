package com.queueless.queueless.service;

import com.queueless.queueless.common.exception.ResourceNotFoundException;
import com.queueless.queueless.dto.notification.NotificationResponse;
import com.queueless.queueless.entity.Notification;
import com.queueless.queueless.entity.NotificationChannel;
import com.queueless.queueless.entity.NotificationStatus;
import com.queueless.queueless.entity.NotificationType;
import com.queueless.queueless.entity.User;
import com.queueless.queueless.repository.NotificationRepository;
import com.queueless.queueless.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationRealtimeService notificationRealtimeService;
    private final EmailNotificationSender emailNotificationSender;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            NotificationRealtimeService notificationRealtimeService,
            EmailNotificationSender emailNotificationSender
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationRealtimeService = notificationRealtimeService;
        this.emailNotificationSender = emailNotificationSender;
    }

    @Transactional
    public void createNotification(User user, NotificationType type, String title, String message) {
        Notification inAppNotification = saveNotification(
                user,
                type,
                NotificationChannel.IN_APP,
                NotificationStatus.SENT,
                title,
                message,
                user.getEmail()
        );
        NotificationResponse response = toResponse(inAppNotification);
        notificationRealtimeService.publish(user, response);
        dispatchEmailIfEnabled(user, type, title, message);
        log.info("Notification created -> type={}, user={}, notificationId={}", type, user.getEmail(), inAppNotification.getId());
    }

    public List<NotificationResponse> getCurrentUserNotifications(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationResponse> getRecentNotificationsForAdmin() {
        return notificationRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public SseEmitter subscribe(Principal principal) {
        return notificationRealtimeService.subscribe(getCurrentUser(principal));
    }

    public User getCurrentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void dispatchEmailIfEnabled(User user, NotificationType type, String title, String message) {
        if (!emailNotificationSender.isEnabled()) {
            return;
        }

        boolean delivered = emailNotificationSender.send(user, title, message);
        saveNotification(
                user,
                type,
                NotificationChannel.EMAIL,
                delivered ? NotificationStatus.SENT : NotificationStatus.FAILED,
                title,
                message,
                user.getEmail()
        );
    }

    private Notification saveNotification(
            User user,
            NotificationType type,
            NotificationChannel channel,
            NotificationStatus status,
            String title,
            String message,
            String recipient
    ) {
        return notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .channel(channel)
                .status(status)
                .title(title)
                .message(message)
                .recipient(recipient)
                .build());
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getChannel(),
                notification.getStatus(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRecipient(),
                notification.getCreatedAt()
        );
    }
}
