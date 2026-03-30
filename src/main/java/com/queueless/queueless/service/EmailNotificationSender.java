package com.queueless.queueless.service;

import com.queueless.queueless.config.AppProperties;
import com.queueless.queueless.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    public EmailNotificationSender(JavaMailSender mailSender, AppProperties appProperties) {
        this.mailSender = mailSender;
        this.appProperties = appProperties;
    }

    public boolean send(User user, String title, String message) {
        if (!appProperties.notifications().email().enabled()) {
            return false;
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(appProperties.notifications().email().from());
        mail.setTo(user.getEmail());
        mail.setSubject(appProperties.notifications().email().subjectPrefix() + " " + title);
        mail.setText(message);

        try {
            mailSender.send(mail);
            log.info("Email notification sent to {}", user.getEmail());
            return true;
        } catch (Exception exception) {
            log.warn("Email notification failed for {}: {}", user.getEmail(), exception.getMessage());
            return false;
        }
    }

    public boolean isEnabled() {
        return appProperties.notifications().email().enabled();
    }
}
