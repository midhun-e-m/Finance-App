# === STAGE 1: Build ===
# Use Maven and Java 23 to compile the code
FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies first (caching optimization)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the actual source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# === STAGE 2: Run ===
# Use a lightweight Java 23 runtime environment
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app

# Copy ONLY the compiled .jar file from the build stage
COPY --from=build /app/target/FinApp-0.0.1-SNAPSHOT.jar app.jar

# Expose the standard Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]