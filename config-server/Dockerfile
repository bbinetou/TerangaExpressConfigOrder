# Dockerfile pour Config Server
FROM openjdk:17-jdk-slim

# Métadonnées
LABEL maintainer="Order Service Team"
LABEL description="Config Server pour système de livraison microservices"
LABEL version="1.0.0"

# Variables d'environnement
ENV APP_HOME=/app
ENV SERVER_PORT=8888

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
COPY config-repo ./config-repo

# Compiler l'application
RUN ./mvnw clean package -DskipTests

# Exposer le port
EXPOSE $SERVER_PORT

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:$SERVER_PORT/actuator/health || exit 1

# Lancer l'application
ENTRYPOINT ["java", "-jar", "target/config-server-0.0.1-SNAPSHOT.jar"]
