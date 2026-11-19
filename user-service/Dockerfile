FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY . .
RUN mvn -B -ntp -pl user-service -am package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/user-service/target/user-service-0.0.1-SNAPSHOT.jar app.jar

USER 10001:10001
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]