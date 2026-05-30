# build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q
# Agente OpenTelemetry (instrumentação zero-code de HTTP, JDBC e Kafka)
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar

# runtime stage
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache tzdata curl
ENV TZ=America/Maceio
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /otel/opentelemetry-javaagent.jar opentelemetry-javaagent.jar
EXPOSE 8089
ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "app.jar"]
