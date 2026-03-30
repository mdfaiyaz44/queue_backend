package com.queueless.queueless.service;

import com.queueless.queueless.common.exception.BadRequestException;
import com.queueless.queueless.common.exception.ResourceNotFoundException;
import com.queueless.queueless.dto.auth.AuthResponse;
import com.queueless.queueless.dto.auth.LoginRequest;
import com.queueless.queueless.dto.auth.RegisterRequest;
import com.queueless.queueless.dto.auth.UserProfileResponse;
import com.queueless.queueless.entity.Role;
import com.queueless.queueless.entity.User;
import com.queueless.queueless.repository.UserRepository;
import com.queueless.queueless.security.CustomUserDetails;
import com.queueless.queueless.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already registered");
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new BadRequestException("Phone number is already registered");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .phone(request.phone())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.PATIENT)
                .active(true)
                .build();
        User savedUser = userRepository.save(user);
        log.info("New patient registered with email {}", savedUser.getEmail());
        return toProfile(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password())
        );

        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);
        log.info("User logged in: {}", user.getEmail());

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole());
    }

    public UserProfileResponse getCurrentUserProfile(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toProfile(user);
    }

    private UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
