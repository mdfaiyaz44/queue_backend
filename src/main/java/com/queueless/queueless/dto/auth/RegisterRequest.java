package com.queueless.queueless.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name is required")
        String name,
        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required")
        String email,
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be exactly 10 digits")
        String phone,
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {
}
