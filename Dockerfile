# Use Java 21
FROM eclipse-temurin:21-jdk-jammy

# Set working directory inside container
WORKDIR /app

# Copy the JAR file into container
# Make sure your JAR file is named 'app.jar' or update this line
COPY xml_importer-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080
# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Command to run the application
CMD ["java", "-jar", "app.jar"]