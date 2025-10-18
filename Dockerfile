FROM bellsoft/liberica-openjdk-alpine:17.0.10 AS builder

WORKDIR /app
RUN apk add --no-cache curl bash git openssh-client

# Copy everything under backend/
COPY backend/ ./backend/

# Give execute permission to wrapper
RUN chmod +x backend/mvnw

# Build the project
RUN cd backend && ./mvnw clean package -DskipTests

# ---------- Runtime stage ----------
FROM bellsoft/liberica-openjdk-alpine:17.0.10
WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /app/backend/target/*.jar /app/app.jar

EXPOSE 8080
EXPOSE 80
EXPOSE 443
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
