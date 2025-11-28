# Build stage
FROM gradle:7.6.6-jdk11 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle ./gradle
COPY --chown=gradle:gradle src ./src

# Download dependencies first (for better caching)
RUN gradle dependencies --no-daemon || true

# Build the JAR with full error output
RUN gradle jar --no-daemon --stacktrace --info 2>&1 | tee /tmp/build.log || (cat /tmp/build.log && exit 1)

# Runtime stage
FROM eclipse-temurin:11-jre-jammy
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
