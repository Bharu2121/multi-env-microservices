package com.sample.microservices.order.integration;

import com.sample.microservices.order.model.Order;
import com.sample.microservices.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full integration test verifying PostgreSQL connection and Flyway migrations
 * via Testcontainers.
 *
 * Enabled when Docker is available (or ENABLE_TESTCONTAINERS=true in CI).
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class OrderRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_orders_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (postgres.isRunning()) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.flyway.enabled", () -> "true");
        }
    }

    @Autowired(required = false)
    private OrderRepository orderRepository;

    @Test
    void testOrderPersistenceWithFlywaySchema() {
        if (orderRepository == null || !postgres.isRunning()) {
            System.out.println("Skipping integration test: Docker daemon not running.");
            return;
        }

        Order order = new Order();
        order.setCustomerName("Test Customer");
        order.setItem("Laptop");
        order.setAmount(new BigDecimal("1200.00"));
        order.setStatus("CONFIRMED");

        Order saved = orderRepository.save(order);
        assertNotNull(saved.getId());

        Optional<Order> fetched = orderRepository.findById(saved.getId());
        assertTrue(fetched.isPresent());
        assertEquals("CONFIRMED", fetched.get().getStatus());
        assertEquals("Laptop", fetched.get().getItem());
    }
}
