package com.queueless.queueless.dto.auth;

import com.queueless.queueless.entity.Role;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        UUID userId,
        String name,
        String email,
        String phone,
        Role role
) {
}
