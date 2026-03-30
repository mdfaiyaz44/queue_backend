package com.queueless.queueless.dto.queue;

import com.queueless.queueless.entity.PriorityLevel;
import com.queueless.queueless.entity.QueueStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record QueueTokenResponse(
        UUID tokenId,
        UUID appointmentId,
        Integer tokenNumber,
        QueueStatus queueStatus,
        PriorityLevel priority,
        Integer predictedWaitMinutes,
        Integer queuePosition,
        String patientName,
        String doctorName,
        LocalDateTime recommendedArrivalTime,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
}
