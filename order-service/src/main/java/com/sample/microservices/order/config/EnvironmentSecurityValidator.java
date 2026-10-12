package com.sample.microservices.order.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Validates critical environment variables at startup and enforces security
 * boundaries between environments.
 */
@Component
public class EnvironmentSecurityValidator {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentSecurityValidator.class);

    private final Environment environment;

    @Value("${spring.datasource.url:}")
    private String dbUrl;

    @Value("${spring.datasource.username:}")
    private String dbUser;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    public EnvironmentSecurityValidator(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validateConfiguration() {
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isTestProfile = Arrays.asList(activeProfiles).contains("test");

        // Skip strict external DB checks when running isolated tests
        if (isTestProfile) {
            log.info("Running under test profile, skipping strict prod/staging validation.");
            return;
        }

        // Fail-fast on required DB credentials
        if (dbUrl == null || dbUrl.isBlank()) {
            throw new IllegalStateException("CRITICAL STARTUP FAILURE: Required environment variable DB_URL is missing!");
        }
        if (dbUser == null || dbUser.isBlank()) {
            throw new IllegalStateException("CRITICAL STARTUP FAILURE: Required environment variable DB_USER is missing!");
        }
        if (dbPassword == null || dbPassword.isBlank()) {
            throw new IllegalStateException("CRITICAL STARTUP FAILURE: Required environment variable DB_PASSWORD is missing!");
        }

        // Environment cross-contamination check: Staging must never point to a production DB
        boolean isStaging = Arrays.asList(activeProfiles).contains("staging");
        if (isStaging && dbUrl.toLowerCase().contains("prod")) {
            throw new IllegalStateException(
                    "CRITICAL SECURITY VIOLATION: Staging profile cannot use a production database host! Target URL was: " + dbUrl
            );
        }

        log.info("Environment configuration validated successfully for active profiles: {}", Arrays.toString(activeProfiles));
    }
}
