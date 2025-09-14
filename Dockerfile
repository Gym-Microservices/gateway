# Multi-stage Docker build for Gateway Service

# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy all POM files
COPY parent/pom.xml /app/parent/pom.xml
COPY gateway/pom.xml /app/gateway/pom.xml

# Install parent POM
RUN cd /app/parent && mvn install -N

# Download microservice dependencies
RUN mkdir -p /app/gateway/src/main/java/temp && \
    echo "public class Temp {}" > /app/gateway/src/main/java/temp/Temp.java

RUN cd /app/gateway && mvn dependency:go-offline -DskipTests

# Clean temp files
RUN rm -rf /app/gateway/src/main/java/temp

# Build gateway service
COPY gateway/src /app/gateway/src
RUN cd /app/gateway && mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/gateway/target/gateway-*.jar app.jar

# Expose port
EXPOSE 8087

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
