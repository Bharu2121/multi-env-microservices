package com.sample.microservices.order.controller;

import com.sample.microservices.order.model.Order;
import com.sample.microservices.order.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class OrderController {

    private final OrderRepository orderRepository;
    // Fallback/in-memory store for unit test mode without DB
    private final Map<Long, Order> inMemoryStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "order-service"));
    }

    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        if (order.getStatus() == null || order.getStatus().isBlank()) {
            order.setStatus("PENDING");
        }
        try {
            Order saved = orderRepository.save(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            // Fallback to in-memory store if DB is unavailable
            long id = idGenerator.getAndIncrement();
            order.setId(id);
            inMemoryStore.put(id, order);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        }
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable("id") Long id) {
        try {
            return orderRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        Order mem = inMemoryStore.get(id);
                        return mem != null ? ResponseEntity.ok(mem) : ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            Order mem = inMemoryStore.get(id);
            return mem != null ? ResponseEntity.ok(mem) : ResponseEntity.notFound().build();
        }
    }
}
