FROM eclipse-temurin:21-jdk as builder
WORKDIR /workspace/app

COPY . .
RUN chmod +x ./gradlew

RUN ./gradlew :rate-limiter-service:build -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/app/rate-limiter-service/build/libs/*.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
