# ---------- Build stage ----------
FROM bellsoft/liberica-openjdk-alpine:17.0.10 AS builder

# Set working directory
WORKDIR /app

# Install basic tools (no global maven needed, since we use mvnw)
RUN apk add --no-cache curl bash git openssh-client

# Copy everything from Backend folder
COPY Backend/ ./Backend/

# Give execute permission to mvnw
RUN chmod +x Backend/mvnw

# Build the project (skip tests for faster deploy)
RUN cd Backend && ./mvnw clean package -DskipTests

# ---------- Runtime stage ----------
FROM bellsoft/liberica-openjdk-alpine:17.0.10

WORKDIR /app

# Copy only the built JAR into final container
COPY --from=builder /app/Backend/target/*.jar /app/app.jar

# Expose ports Dokku will map
EXPOSE 8080
EXPOSE 443
EXPOSE 80

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
