# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM maven:3.9.10-eclipse-temurin-11 AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests clean package

# ---- Runtime stage ----
FROM eclipse-temurin:11-jre
WORKDIR /app

ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseSerialGC"
ENV PORT=8080

COPY --from=builder /app/target/pdf-convert-demo-0.0.1-SNAPSHOT.jar app.jar

# Create writable local data directories for demo outputs.
RUN mkdir -p /app/data/uploads /app/data/pages /app/data/normalized /app/data/temp

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]
