# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY checkstyle.xml .

RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package

# Run stage
FROM openjdk:17-jdk-slim
COPY --from=build /app/target/spring-rest-backend-0.0.1.jar /usr/local/lib/app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/usr/local/lib/app.jar"]
