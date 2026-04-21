# Build stage
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradle.properties* ./
COPY gradle ./gradle
COPY src ./src

RUN gradle build -x test --no-daemon

# Production stage
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Create media directory for files
RUN mkdir -p /app/media

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
