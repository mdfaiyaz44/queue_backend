package com.queueless.queueless.dto.prediction;

import java.time.LocalDateTime;
import java.util.UUID;

public record WaitTimePredictionResponse(
        UUID doctorId,
        int waitingPatients,
        int emergencyPatientsAhead,
        int predictedWaitMinutes,
        LocalDateTime estimatedConsultationStart,
        LocalDateTime recommendedArrivalTime,
        String predictionMode
) {
}
