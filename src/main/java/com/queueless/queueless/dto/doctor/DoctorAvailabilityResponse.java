package com.queueless.queueless.dto.doctor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record DoctorAvailabilityResponse(
        UUID id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Integer slotDurationMinutes,
        Integer maxPatients
) {
}
