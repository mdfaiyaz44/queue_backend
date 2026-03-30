package com.queueless.queueless.dto.doctor;

import java.util.List;
import java.util.UUID;

public record DoctorResponse(
        UUID id,
        UUID userId,
        String doctorName,
        String doctorEmail,
        String doctorPhone,
        UUID departmentId,
        String departmentName,
        String qualification,
        String licenseNumber,
        Integer experienceYears,
        Integer averageConsultationMinutes,
        Integer consultationBufferMinutes,
        boolean acceptingAppointments,
        List<DoctorAvailabilityResponse> availability
) {
}
