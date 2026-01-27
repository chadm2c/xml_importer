FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /workspace/target/xml_importer-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
RUN groupadd -r spring && useradd -r -g spring spring
USER spring
CMD ["java", "-jar", "app.jar"]
