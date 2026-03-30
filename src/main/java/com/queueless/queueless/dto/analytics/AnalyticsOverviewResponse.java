package com.queueless.queueless.dto.analytics;

import java.time.LocalDate;
import java.util.Map;

public record AnalyticsOverviewResponse(
        LocalDate date,
        long totalDoctors,
        long totalPatients,
        long totalAppointments,
        long waitingTokens,
        long inProgressTokens,
        long completedAppointments,
        Map<String, Long> appointmentsByDepartment
) {
}
