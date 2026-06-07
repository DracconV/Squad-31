# build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# runtime stage
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache tzdata curl
ENV TZ=America/Maceio
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
