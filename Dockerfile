FROM gradle:8.10-jdk21 AS build

WORKDIR /app
COPY . .

RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
COPY src/main/resources/logback.xml /app/config/
RUN mkdir -p /run/secrets
ENV CONFIG_SOURCE=env

CMD ["java", "-jar", "app.jar"]