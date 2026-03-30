package com.queueless.queueless.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Bootstrap bootstrap,
        Features features,
        Notifications notifications
) {
    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Bootstrap(Admin admin) {
        public record Admin(String email, String password, String name, String phone) {
        }
    }

    public record Features(boolean redisEnabled, boolean kafkaEnabled, boolean aiEnabled) {
    }

    public record Notifications(boolean realtimeEnabled, Email email) {
        public record Email(boolean enabled, String from, String subjectPrefix) {
        }
    }
}
