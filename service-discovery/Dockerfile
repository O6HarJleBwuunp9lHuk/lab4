FROM eclipse-temurin:21-jdk as builder
WORKDIR /workspace/app

COPY . .
RUN chmod +x ./gradlew

RUN ./gradlew :service-discovery:build -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/app/service-discovery/build/libs/*.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
