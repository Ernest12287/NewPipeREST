# Build stage
FROM gradle:7.6.6-jdk11 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle ./gradle
COPY --chown=gradle:gradle src ./src

# Download dependencies first (for better caching)
RUN gradle dependencies --no-daemon

# Build the JAR
RUN gradle jar --no-daemon --stacktrace

# Runtime stage
FROM eclipse-temurin:11-jre-jammy
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
