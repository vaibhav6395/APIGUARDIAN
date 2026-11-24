# Build the React UI and package it inside Spring Boot so a hosting platform
# needs only one web service plus a managed PostgreSQL database.
FROM node:22-alpine AS frontend-build
WORKDIR /workspace/frontend
RUN npm install --global npm@11.21.3
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /workspace/backend
COPY backend/pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline
COPY backend/src ./src
COPY --from=frontend-build /workspace/frontend/dist ./src/main/resources/static
RUN mvn -q -DskipTests package

FROM eclipse-temurin:25-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl     && rm -rf /var/lib/apt/lists/*     && groupadd --system app && useradd --system --gid app --home-dir /app app
WORKDIR /app
COPY --from=backend-build /workspace/backend/target/apiguardian-api-0.2.0-SNAPSHOT.jar /app/app.jar
USER app
ENV PORT=8080
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD curl --fail --silent http://127.0.0.1:${PORT}/actuator/health/readiness || exit 1
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]
