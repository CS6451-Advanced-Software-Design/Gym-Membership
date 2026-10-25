# Builds the jar. The tests already ran in CI, so they and the reports are skipped here
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q package -DskipTests -Dpmd.skip -Djacoco.skip

# Runs the jar on a JRE. The JSON files live in /data, where Railway mounts a volume so they survive redeploys
FROM eclipse-temurin:21-jre-noble
WORKDIR /app
COPY --from=build /app/target/gym-membership-*.jar app.jar
ENV GYM_PERSISTENCE_DIRECTORY=/data
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
