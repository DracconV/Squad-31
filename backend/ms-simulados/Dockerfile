FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q
# Agente OpenTelemetry (instrumentação zero-code de HTTP, JDBC, Redis e Kafka)
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar

FROM eclipse-temurin:21-jre-alpine
# curl é necessário para o healthcheck do docker-compose
RUN apk add --no-cache tzdata curl
ENV TZ=America/Maceio
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /otel/opentelemetry-javaagent.jar opentelemetry-javaagent.jar
EXPOSE 8088
ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "app.jar"]
