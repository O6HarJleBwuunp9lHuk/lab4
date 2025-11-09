FROM eclipse-temurin:21-jdk as builder
WORKDIR /workspace/app

COPY . .
RUN chmod +x ./gradlew

RUN ./gradlew :notification-service:build -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/app/notification-service/build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
