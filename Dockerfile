FROM eclipse-temurin:24-jdk-alpine
WORKDIR /app

# Create directory for media files
RUN mkdir -p /app/media && chmod 775 /app/media

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Change ownership of the app directory to appuser
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

COPY build/libs/stackquiz-api-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
