package com.queueless.queueless.dto.appointment;

import com.queueless.queueless.entity.PriorityLevel;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull(message = "Doctor id is required")
        UUID doctorId,
        @NotNull(message = "Appointment date is required")
        @FutureOrPresent(message = "Appointment date cannot be in the past")
        LocalDate appointmentDate,
        @NotNull(message = "Appointment time is required")
        LocalTime appointmentTime,
        @NotNull(message = "Priority is required")
        PriorityLevel priority,
        @Size(max = 1000, message = "Symptoms should be within 1000 characters")
        String symptoms,
        @Size(max = 1000, message = "Notes should be within 1000 characters")
        String notes
) {
}
