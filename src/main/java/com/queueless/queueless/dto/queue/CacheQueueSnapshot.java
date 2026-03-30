package com.queueless.queueless.dto.queue;

import java.time.LocalDateTime;

public record CacheQueueSnapshot(
        DoctorQueueResponse queue,
        LocalDateTime cachedAt
) {
}
