package com.queueless.queueless.dto.queue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DoctorQueueResponse(
        UUID doctorId,
        String doctorName,
        LocalDate queueDate,
        long waitingCount,
        long inProgressCount,
        long completedCount,
        List<QueueTokenResponse> tokens
) {
}
