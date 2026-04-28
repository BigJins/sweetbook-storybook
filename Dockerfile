# ---- Frontend build ----
FROM node:20-alpine AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- Backend build ----
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY backend/pom.xml ./
RUN mvn dependency:go-offline -B
COPY backend/src ./src
COPY --from=frontend-build /app/dist ./src/main/resources/static
RUN mvn -DskipTests package

# ---- Runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/target/sweetbook-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /data/uploads
EXPOSE 8080
ENV UPLOAD_DIR=/data/uploads
ENTRYPOINT ["java","-jar","/app/app.jar"]
