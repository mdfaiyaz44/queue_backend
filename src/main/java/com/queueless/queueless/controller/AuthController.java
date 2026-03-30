package com.queueless.queueless.controller;

import com.queueless.queueless.common.ApiResponse;
import com.queueless.queueless.dto.auth.AuthResponse;
import com.queueless.queueless.dto.auth.LoginRequest;
import com.queueless.queueless.dto.auth.RegisterRequest;
import com.queueless.queueless.dto.auth.UserProfileResponse;
import com.queueless.queueless.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> currentUser(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Current user fetched successfully", authService.getCurrentUserProfile(principal)));
    }
}
