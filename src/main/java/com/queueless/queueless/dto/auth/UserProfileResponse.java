package com.queueless.queueless.dto.auth;

import com.queueless.queueless.entity.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String name,
        String email,
        String phone,
        Role role,
        boolean active,
        LocalDateTime createdAt
) {
}
