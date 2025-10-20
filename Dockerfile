FROM eclipse-temurin:24-jdk-alpine
WORKDIR /app

# Create directory for media files
RUN mkdir -p /app/media && chmod 775 /app/media

COPY build/libs/stackquiz-api-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
