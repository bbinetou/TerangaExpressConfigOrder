# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copier les fichiers Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Rendre mvnw exécutable
RUN chmod +x mvnw

# Télécharger les dépendances
RUN ./mvnw dependency:go-offline -B || true

# Copier le code source
COPY src ./src

# Compiler l'application
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Order Service Team"
LABEL description="Order Service pour système de livraison microservices"
LABEL version="1.0.0"

# Variables d'environnement
ENV SERVER_PORT=8084

WORKDIR /app

# Installer curl pour les health checks
RUN apk add --no-cache curl

# Copier le JAR depuis le stage de build
COPY --from=builder /app/target/order-service-0.0.1-SNAPSHOT.jar app.jar

# Exposer le port
EXPOSE $SERVER_PORT

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:$SERVER_PORT/actuator/health || exit 1

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
