# Dockerfile pour Order Service
FROM openjdk:17-jdk-slim

# Métadonnées
LABEL maintainer="Order Service Team"
LABEL description="Order Service pour système de livraison microservices"
LABEL version="1.0.0"

# Variables d'environnement
ENV APP_HOME=/app
ENV SERVER_PORT=8084
ENV CONFIG_SERVER_URL=http://config-server:8888
ENV EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
ENV POSTGRES_HOST=postgres
ENV POSTGRES_PORT=5432
ENV POSTGRES_DB=order_db
ENV POSTGRES_USER=orderuser
ENV POSTGRES_PASSWORD=orderpass
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Créer le répertoire de l'application
WORKDIR $APP_HOME

# Copier le fichier pom.xml et télécharger les dépendances (pour cache Docker)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Télécharger les dépendances (cette couche sera cachée si pom.xml ne change pas)
RUN ./mvnw dependency:go-offline -B

# Copier le code source
COPY src ./src

# Compiler l'application
RUN ./mvnw clean package -DskipTests

# Exposer le port
EXPOSE $SERVER_PORT

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:$SERVER_PORT/actuator/health || exit 1

# Lancer l'application
ENTRYPOINT ["java", "-jar", "target/order-service-0.0.1-SNAPSHOT.jar"]
