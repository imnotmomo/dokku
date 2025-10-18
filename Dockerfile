FROM bellsoft/liberica-openjdk-alpine:17.0.10 AS builder
WORKDIR /app
RUN apk add --no-cache curl bash git openssh-client
COPY backend/ ./
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests
FROM bellsoft/liberica-openjdk-alpine:17.0.10
WORKDIR /app

EXPOSE 8080
EXPOSE 80
EXPOSE 443

# Command to run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
