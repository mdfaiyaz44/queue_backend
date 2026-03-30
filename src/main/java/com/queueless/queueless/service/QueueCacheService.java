package com.queueless.queueless.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.queueless.queueless.config.AppProperties;
import com.queueless.queueless.dto.queue.CacheQueueSnapshot;
import com.queueless.queueless.dto.queue.DoctorQueueResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class QueueCacheService {

    private static final Logger log = LoggerFactory.getLogger(QueueCacheService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    public QueueCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, AppProperties appProperties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
    }

    public Optional<DoctorQueueResponse> getDoctorQueue(UUID doctorId, LocalDate date) {
        if (!appProperties.features().redisEnabled()) {
            return Optional.empty();
        }

        try {
            String raw = redisTemplate.opsForValue().get(buildDoctorQueueKey(doctorId, date));
            if (raw == null || raw.isBlank()) {
                return Optional.empty();
            }

            CacheQueueSnapshot snapshot = objectMapper.readValue(raw, CacheQueueSnapshot.class);
            log.info("Queue cache hit for doctor {} on {}", doctorId, date);
            return Optional.of(snapshot.queue());
        } catch (Exception exception) {
            log.warn("Queue cache read failed: {}", exception.getMessage());
            return Optional.empty();
        }
    }

    public void cacheDoctorQueue(DoctorQueueResponse response) {
        if (!appProperties.features().redisEnabled()) {
            return;
        }

        try {
            CacheQueueSnapshot snapshot = new CacheQueueSnapshot(response, LocalDateTime.now());
            redisTemplate.opsForValue().set(
                    buildDoctorQueueKey(response.doctorId(), response.queueDate()),
                    objectMapper.writeValueAsString(snapshot),
                    CACHE_TTL
            );
            log.info("Queue cache updated for doctor {} on {}", response.doctorId(), response.queueDate());
        } catch (JsonProcessingException exception) {
            log.warn("Queue cache write failed: {}", exception.getMessage());
        }
    }

    public void evictDoctorQueue(UUID doctorId, LocalDate date) {
        if (!appProperties.features().redisEnabled()) {
            return;
        }
        try {
            redisTemplate.delete(buildDoctorQueueKey(doctorId, date));
            log.info("Queue cache evicted for doctor {} on {}", doctorId, date);
        } catch (Exception exception) {
            log.warn("Queue cache eviction failed: {}", exception.getMessage());
        }
    }

    private String buildDoctorQueueKey(UUID doctorId, LocalDate date) {
        return "queue:doctor:%s:%s".formatted(doctorId, date);
    }
}
