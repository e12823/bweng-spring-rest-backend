# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY checkstyle.xml .

RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package

# Run stage
# openjdk:17-jdk-slim tag has been removed; use a current Temurin image instead
FROM eclipse-temurin:17-jdk
COPY --from=build /app/target/spring-rest-backend-0.0.1.jar /usr/local/lib/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/lib/app.jar"]
