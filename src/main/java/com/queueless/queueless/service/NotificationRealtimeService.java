package com.queueless.queueless.service;

import com.queueless.queueless.config.AppProperties;
import com.queueless.queueless.dto.notification.NotificationResponse;
import com.queueless.queueless.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationRealtimeService {

    private static final Logger log = LoggerFactory.getLogger(NotificationRealtimeService.class);
    private static final long SSE_TIMEOUT_MILLIS = 30L * 60L * 1000L;

    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final AppProperties appProperties;

    public NotificationRealtimeService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public SseEmitter subscribe(User user) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        emitters.computeIfAbsent(user.getId(), key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> unregister(user.getId(), emitter));
        emitter.onTimeout(() -> unregister(user.getId(), emitter));
        emitter.onError(exception -> unregister(user.getId(), emitter));

        if (appProperties.notifications().realtimeEnabled()) {
            try {
                emitter.send(SseEmitter.event()
                        .name("connected")
                        .data("Notification stream connected at " + LocalDateTime.now()));
            } catch (IOException exception) {
                unregister(user.getId(), emitter);
                log.warn("Failed to initialize notification stream for {}: {}", user.getEmail(), exception.getMessage());
            }
        }

        return emitter;
    }

    public void publish(User user, NotificationResponse response) {
        if (!appProperties.notifications().realtimeEnabled()) {
            return;
        }

        List<SseEmitter> userEmitters = emitters.get(user.getId());
        if (userEmitters == null || userEmitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .id(response.id().toString())
                        .name("notification")
                        .data(response));
            } catch (IOException exception) {
                unregister(user.getId(), emitter);
                log.warn("Removed broken notification stream for {}: {}", user.getEmail(), exception.getMessage());
            }
        }
    }

    private void unregister(UUID userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }
        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }
}
