package com.queueless.queueless.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "Department name is required")
        @Size(max = 150, message = "Department name must be within 150 characters")
        String name,
        @Size(max = 1000, message = "Description must be within 1000 characters")
        String description
) {
}
