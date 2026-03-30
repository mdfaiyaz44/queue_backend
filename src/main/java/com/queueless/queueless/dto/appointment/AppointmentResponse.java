package com.queueless.queueless.dto.appointment;

import com.queueless.queueless.entity.AppointmentStatus;
import com.queueless.queueless.entity.PriorityLevel;
import com.queueless.queueless.entity.QueueStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID appointmentId,
        UUID doctorId,
        String doctorName,
        String departmentName,
        UUID patientId,
        String patientName,
        LocalDate appointmentDate,
        LocalTime appointmentTime,
        AppointmentStatus status,
        PriorityLevel priority,
        String symptoms,
        String notes,
        Integer estimatedConsultationMinutes,
        LocalDateTime estimatedStartTime,
        LocalDateTime recommendedArrivalTime,
        UUID tokenId,
        Integer tokenNumber,
        QueueStatus queueStatus,
        Integer predictedWaitMinutes
) {
}
