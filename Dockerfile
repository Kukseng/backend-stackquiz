FROM eclipse-temurin:24-jdk-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Create media directory and give ownership to appuser
RUN mkdir -p /app/media && chown -R appuser:appuser /app && chmod 775 /app/media

# Switch to non-root user
USER appuser

COPY build/libs/stackquiz-api-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]