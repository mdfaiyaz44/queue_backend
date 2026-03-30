package com.queueless.queueless.dto.doctor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DoctorAvailabilityRequest(
        @NotNull(message = "Day of week is required")
        DayOfWeek dayOfWeek,
        @NotNull(message = "Start time is required")
        LocalTime startTime,
        @NotNull(message = "End time is required")
        LocalTime endTime,
        @NotNull(message = "Slot duration is required")
        @Min(value = 5, message = "Slot duration must be at least 5 minutes")
        @Max(value = 60, message = "Slot duration must be 60 minutes or less")
        Integer slotDurationMinutes,
        @NotNull(message = "Max patients is required")
        @Min(value = 1, message = "Max patients must be at least 1")
        @Max(value = 100, message = "Max patients must be 100 or less")
        Integer maxPatients
) {
}
