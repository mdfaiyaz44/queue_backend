package com.queueless.queueless.config;

import com.queueless.queueless.entity.Role;
import com.queueless.queueless.entity.User;
import com.queueless.queueless.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, AppProperties appProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void createAdminIfMissing() {
        String adminEmail = appProperties.bootstrap().admin().email();
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        User admin = User.builder()
                .name(appProperties.bootstrap().admin().name())
                .email(adminEmail)
                .phone(appProperties.bootstrap().admin().phone())
                .password(passwordEncoder.encode(appProperties.bootstrap().admin().password()))
                .role(Role.ADMIN)
                .active(true)
                .build();
        userRepository.save(admin);
        log.info("Bootstrap admin user created with email {}", adminEmail);
    }
}
