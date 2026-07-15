FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY common common/
COPY notification-service notification-service/
RUN mvn -pl notification-service -am clean package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/notification-service/target/*.jar app.jar
EXPOSE 8103
ENTRYPOINT ["java", "-jar", "app.jar"]
