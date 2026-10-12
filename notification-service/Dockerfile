# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .
COPY auth-service/pom.xml auth-service/
COPY order-service/pom.xml order-service/
COPY notification-service/pom.xml notification-service/
RUN mvn -B dependency:go-offline -pl notification-service -am || true

COPY notification-service/src notification-service/src
RUN mvn -B clean package -pl notification-service -am -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd -r appgroup && useradd -r -g appgroup -u 1001 appuser
USER appuser

COPY --from=builder /build/notification-service/target/*.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
