# ===== 1단계: 빌드 =====
FROM gradle:8.14-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

# ===== 2단계: 실행 =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]