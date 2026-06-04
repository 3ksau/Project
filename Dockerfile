FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
    RUN mvn dependency:resolve -B
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
VOLUME /app/uploads
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILE:docker}", "-jar", "app.jar"]
