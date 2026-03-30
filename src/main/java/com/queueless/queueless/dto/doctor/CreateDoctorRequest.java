package com.queueless.queueless.dto.doctor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateDoctorRequest(
        @NotBlank(message = "Doctor name is required")
        String name,
        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required")
        String email,
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be exactly 10 digits")
        String phone,
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,
        @NotNull(message = "Department id is required")
        UUID departmentId,
        @NotBlank(message = "Qualification is required")
        String qualification,
        @NotBlank(message = "License number is required")
        String licenseNumber,
        @NotNull(message = "Experience is required")
        @Min(value = 0, message = "Experience cannot be negative")
        @Max(value = 60, message = "Experience looks too large")
        Integer experienceYears,
        @NotNull(message = "Average consultation time is required")
        @Min(value = 5, message = "Average consultation time must be at least 5 minutes")
        @Max(value = 120, message = "Average consultation time must be below 120 minutes")
        Integer averageConsultationMinutes,
        @NotNull(message = "Consultation buffer is required")
        @Min(value = 0, message = "Buffer cannot be negative")
        @Max(value = 60, message = "Buffer must be below 60 minutes")
        Integer consultationBufferMinutes
) {
}
