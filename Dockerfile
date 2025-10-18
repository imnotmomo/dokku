FROM bellsoft/liberica-openjdk-alpine:17.0.10

WORKDIR /app

RUN apk add curl
RUN apk add bash
RUN apk add maven
RUN apk add --no-cache git openssh-client

EXPOSE 8080
EXPOSE 443
EXPOSE 80
