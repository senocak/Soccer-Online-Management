# Build stage
FROM maven:3.8-openjdk-17 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean install

# Package stage
FROM openjdk:21-ea-17-slim-buster
COPY --from=build /home/app/target/ing-0.0.1.jar /usr/local/lib/ing.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/lib/ing.jar"]