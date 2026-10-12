# Multi-Environment Microservice Sample

A sample microservice monorepo built with Java 17, Spring Boot 3, and Maven, designed for multi-environment deployments (DEV, QA, Staging, Production).

## Services Overview

| Service | Port | Key Endpoints | Description |
|---|---|---|---|
| `auth-service` | 8081 | `GET /health`, `POST /login` | Dummy JWT token generation |
| `order-service` | 8082 | `GET /health`, `POST /orders`, `GET /orders/{id}` | Order processing with Flyway migrations |
| `notification-service` | 8083 | `GET /health`, `POST /notify` | Simple logging notification service |

---

## Local Development Quickstart

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### 1. Build All Services
```bash
mvn clean install
```

### 2. Run Locally with Docker Compose
```bash
docker-compose up --build -d
```

### 3. Check Health Endpoints
```bash
curl http://localhost:8081/health
curl http://localhost:8082/health
curl http://localhost:8083/health
```

### 4. Test Sample Business APIs
```bash
# Auth Service - Login
curl -X POST http://localhost:8081/login \
  -H "Content-Type: application/json" \
  -d '{"username": "johndoe", "password": "password"}'

# Order Service - Create Order
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"customerName": "John Doe", "item": "Mechanical Keyboard", "amount": 129.99}'

# Order Service - Get Order
curl http://localhost:8082/orders/1

# Notification Service - Notify
curl -X POST http://localhost:8083/notify \
  -H "Content-Type: application/json" \
  -d '{"event": "ORDER_CREATED", "orderId": 1}'
```

### 5. Tear Down
```bash
docker-compose down -v
```
