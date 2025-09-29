# Dockerfile pour l'API Spring Boot
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

# Installer curl pour les health checks
RUN apk add --no-cache curl

WORKDIR /app

# Copier le JAR depuis le stage de build
COPY --from=builder /app/target/*.jar app.jar

# Créer un utilisateur non-root
RUN adduser -D -s /bin/sh appuser
USER appuser

# Variables d'environnement
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8080

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]