package com.sample.microservices.order.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentSecurityValidatorTest {

    @Test
    void shouldFailWhenRequiredDbVarsMissing() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");
        EnvironmentSecurityValidator validator = new EnvironmentSecurityValidator(env);
        ReflectionTestUtils.setField(validator, "dbUrl", "");
        ReflectionTestUtils.setField(validator, "dbUser", "");
        ReflectionTestUtils.setField(validator, "dbPassword", "");

        IllegalStateException ex = assertThrows(IllegalStateException.class, validator::validateConfiguration);
        assertTrue(ex.getMessage().contains("Required environment variable DB_URL is missing"));
    }

    @Test
    void shouldBlockStagingFromUsingProdDatabase() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("staging");
        EnvironmentSecurityValidator validator = new EnvironmentSecurityValidator(env);
        ReflectionTestUtils.setField(validator, "dbUrl", "jdbc:postgresql://orders-prod-db.internal:5432/orders");
        ReflectionTestUtils.setField(validator, "dbUser", "order_user");
        ReflectionTestUtils.setField(validator, "dbPassword", "some_secret");

        IllegalStateException ex = assertThrows(IllegalStateException.class, validator::validateConfiguration);
        assertTrue(ex.getMessage().contains("Staging profile cannot use a production database host"));
    }
}
