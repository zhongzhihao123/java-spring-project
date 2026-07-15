FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY common common/
COPY user-service user-service/
RUN mvn -pl user-service -am clean package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/user-service/target/*.jar app.jar
EXPOSE 8101
ENTRYPOINT ["java", "-jar", "app.jar"]
