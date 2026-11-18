# The image Cloud Run runs. Build the jar first with `./gradlew bootJar`: this only copies it in.
FROM eclipse-temurin:25-jre

COPY build/libs/*.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
