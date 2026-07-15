FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY common common/
COPY business-service business-service/
RUN mvn -pl business-service -am clean package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/business-service/target/*.jar app.jar
EXPOSE 8102
ENTRYPOINT ["java", "-jar", "app.jar"]
