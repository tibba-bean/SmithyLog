# Use the official Maven image as the base image
FROM maven:3.8.6-openjdk-11 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and the source code into the container
COPY pom.xml ./
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Use a lightweight image for the final application
FROM openjdk:11-jre-slim

# Set the working directory for the final image
WORKDIR /app

# Copy the packaged JAR file from the build stage
COPY --from=build /app/target/SmithyLog-1.0-SNAPSHOT.jar app.jar

# Expose the application port (change this if your app runs on a different port)
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "app.jar"]
