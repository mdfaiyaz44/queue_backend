package com.queueless.queueless.dto.department;

import java.time.LocalDateTime;
import java.util.UUID;

public record DepartmentResponse(
        UUID id,
        String name,
        String description,
        LocalDateTime createdAt
) {
}
