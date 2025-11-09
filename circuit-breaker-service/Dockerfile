FROM eclipse-temurin:21-jdk as builder
WORKDIR /workspace/app

COPY . .
RUN chmod +x ./gradlew

RUN ./gradlew :circuit-breaker-service:build -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/app/circuit-breaker-service/build/libs/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
