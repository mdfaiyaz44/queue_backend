package com.queueless.queueless.dto.event;

import com.queueless.queueless.entity.PriorityLevel;
import com.queueless.queueless.entity.QueueStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record QueueEventPayload(
        String eventType,
        UUID tokenId,
        UUID appointmentId,
        UUID doctorId,
        UUID patientId,
        Integer tokenNumber,
        QueueStatus queueStatus,
        PriorityLevel priority,
        LocalDate appointmentDate,
        LocalDateTime occurredAt
) {
}
