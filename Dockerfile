# ---------- Stage 1: build ----------
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline -q || true
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV PORT=8081
ENV JAVA_OPTS="-Xmx256m"
# Secrets are injected via Render env vars at runtime - never bake .env into the image.
COPY --from=build /app/target/quiz-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]