FROM bellsoft/liberica-openjdk-alpine:17.0.10 AS builder

WORKDIR /app

RUN apk add --no-cache curl bash git openssh-client



RUN chmod +x Backend/mvnw

RUN cd Backend && ./mvnw clean package -DskipTests

FROM bellsoft/liberica-openjdk-alpine:17.0.10

WORKDIR /app

COPY --from=builder /app/Backend/target/*.jar /app/app.jar

EXPOSE 8080
EXPOSE 443
EXPOSE 80

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
