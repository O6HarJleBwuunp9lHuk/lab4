FROM eclipse-temurin:21-jdk as builder
WORKDIR /workspace/app

COPY . .
RUN chmod +x ./gradlew

RUN ./gradlew :user-service:build -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/app/user-service/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
