# Système de Livraison Microservices - Guide Complet

> 👋 **Nouveau sur le projet?** Commencez par [WELCOME.md](WELCOME.md) pour un accueil complet!

## 📋 Table des Matières
- [Vue d'ensemble](#vue-densemble)
- [Architecture](#architecture)
- [Services Implémentés](#services-implémentés)
- [Prérequis](#prérequis)
- [Installation et Configuration](#installation-et-configuration)
- [Démarrage du Système](#démarrage-du-système)
- [Tests des Endpoints](#tests-des-endpoints)
- [Communication Inter-Services](#communication-inter-services)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)

## 📚 Documentation Complète

Ce README est le guide principal. Pour une navigation complète de toute la documentation:

- **[INDEX.md](INDEX.md)** - Index complet de toute la documentation
- **[QUICK_START.md](QUICK_START.md)** - Démarrage rapide en 5 minutes
- **[INSTALLATION_GUIDE.md](INSTALLATION_GUIDE.md)** - Guide d'installation PostgreSQL, Kafka, Zookeeper
- **[DOCKER_GUIDE.md](DOCKER_GUIDE.md)** - Guide complet Docker et docker-compose
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Résumé technique détaillé
- **[COMMANDS_CHEATSHEET.md](COMMANDS_CHEATSHEET.md)** - Aide-mémoire des commandes
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Guide de contribution
- **[FINAL_SUMMARY.md](FINAL_SUMMARY.md)** - Résumé final du projet

---

## 🎯 Vue d'ensemble

Système de livraison distribué basé sur une architecture microservices avec Spring Cloud. Le système permet la gestion complète des commandes de livraison, du suivi en temps réel, des paiements et de l'orchestration des chauffeurs.

### Fonctionnalités Principales
- ✅ Configuration centralisée (Config Server)
- ✅ Découverte de services (Eureka)
- ✅ Orchestration des commandes (Order Service)
- ✅ Communication asynchrone (Kafka)
- ✅ Gestion des erreurs standardisée
- ✅ Monitoring et health checks

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    API Gateway (8080)                    │
│              (Point d'entrée unique)                     │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────┐
│              Eureka Server (8761)                        │
│           (Découverte de services)                       │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
┌───────▼──────┐ ┌──▼──────┐ ┌──▼──────────┐
│Config Server │ │  Order  │ │   Autres    │
│   (8888)     │ │ Service │ │  Services   │
│              │ │ (8084)  │ │  (futurs)   │
└──────────────┘ └────┬────┘ └─────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
┌───────▼──────┐ ┌───▼────────┐ ┌─▼──────────┐
│ PostgreSQL   │ │   Kafka    │ │   Feign    │
│  (order_db)  │ │  (9092)    │ │  Clients   │
└──────────────┘ └────────────┘ └────────────┘
```

---

## 📦 Services Implémentés

### 1. Config Server (Port 8888)
**Statut:** ✅ Complet

**Responsabilité:** Configuration centralisée pour tous les services

**Fichiers de configuration:**
- `application.yml` - Configuration commune
- `order-service.yml` - Configuration spécifique order-service
- `eureka-server.yml` - Configuration Eureka
- `api-gateway.yml` - Configuration API Gateway
- Et configurations pour tous les autres services (auth, user, parcel, payment, driver, tracking, grouping, notification)

**Endpoints:**
- `GET http://localhost:8888/{service-name}/{profile}` - Récupérer la configuration

### 2. Order Service (Port 8084)
**Statut:** ✅ Complet

**Responsabilité:** Orchestration des commandes de livraison

**Fonctionnalités implémentées:**
- ✅ Création de commandes avec validation du colis
- ✅ Confirmation de commandes (après paiement)
- ✅ Annulation avec compensation (remboursement)
- ✅ Assignation de chauffeurs avec vérification de disponibilité
- ✅ Gestion des événements de suivi
- ✅ Gestion des paiements
- ✅ Historique des commandes par client
- ✅ Publication d'événements Kafka
- ✅ Communication Feign avec autres services
- ✅ Gestion des erreurs standardisée

**Endpoints:** Voir [order-service/API.md](order-service/API.md)

**Base de données:** PostgreSQL (order_db)

**Tables:**
- `orders` - Commandes de livraison
- `payments` - Paiements
- `tracking_events` - Événements de suivi
- `delivery_groups` - Groupements de livraisons

---

## 🔧 Prérequis

### Logiciels Requis

**Option 1: Avec Docker (Recommandé)**

1. **Docker Desktop 20.10+**
   - Windows/Mac: https://www.docker.com/products/docker-desktop
   - Linux: Voir [INSTALLATION_GUIDE.md](INSTALLATION_GUIDE.md)

2. **Docker Compose 2.0+** (inclus avec Docker Desktop)

**Option 2: Installation Manuelle**

1. **Java 17+**
   ```bash
   java -version
   # Doit afficher: openjdk version "17" ou supérieur
   ```

2. **Maven 3.8+**
   ```bash
   mvn -version
   ```

3. **PostgreSQL 14+**
   ```bash
   psql --version
   ```

4. **Apache Kafka 3.x** (optionnel pour tests complets)
   - Télécharger: https://kafka.apache.org/downloads

5. **Git**
   ```bash
   git --version
   ```

> 📖 **Guide d'installation détaillé**: [INSTALLATION_GUIDE.md](INSTALLATION_GUIDE.md)

---

## 🚀 Installation et Configuration

### Option 1: Avec Docker (Recommandé) 🐳

#### Démarrage Rapide

```bash
# 1. Cloner le projet
git clone <repository-url>
cd delivery-microservices-system

# 2. Démarrer tous les services avec Docker Compose
docker-compose up -d

# 3. Vérifier le statut
docker-compose ps

# 4. Voir les logs
docker-compose logs -f
```

**C'est tout!** Tous les services (PostgreSQL, Kafka, Zookeeper, Config Server, Order Service) sont maintenant démarrés.

#### Avec Makefile (encore plus simple)

```bash
# Démarrage complet
make quick-start

# Vérifier la santé
make health

# Voir les logs
make logs

# Arrêter
make down
```

> 📖 **Guide Docker complet**: [DOCKER_GUIDE.md](DOCKER_GUIDE.md)

---

### Option 2: Installation Manuelle

#### Étape 1: Cloner le Projet

```bash
git clone <repository-url>
cd delivery-microservices-system
```

#### Étape 2: Configuration PostgreSQL

#### 2.1 Créer la base de données

```sql
-- Se connecter à PostgreSQL
psql -U postgres

-- Créer l'utilisateur
CREATE USER orderuser WITH PASSWORD 'orderpass';

-- Créer la base de données
CREATE DATABASE order_db;

-- Donner les privilèges
GRANT ALL PRIVILEGES ON DATABASE order_db TO orderuser;

-- Se connecter à la base
\c order_db

-- Donner les privilèges sur le schéma
GRANT ALL ON SCHEMA public TO orderuser;
```

#### 2.2 Vérifier la connexion

```bash
psql -U orderuser -d order_db -h localhost
# Mot de passe: orderpass
```

### Étape 3: Configuration Kafka (Optionnel)

#### 3.1 Démarrer Zookeeper

```bash
# Windows
bin\windows\zookeeper-server-start.bat config\zookeeper.properties

# Linux/Mac
bin/zookeeper-server-start.sh config/zookeeper.properties
```

#### 3.2 Démarrer Kafka

```bash
# Windows
bin\windows\kafka-server-start.bat config\server.properties

# Linux/Mac
bin/kafka-server-start.sh config/server.properties
```

#### 3.3 Créer les topics

```bash
# Windows
bin\windows\kafka-topics.bat --create --topic order.created --bootstrap-server localhost:9092
bin\windows\kafka-topics.bat --create --topic order.confirmed --bootstrap-server localhost:9092
bin\windows\kafka-topics.bat --create --topic order.assigned --bootstrap-server localhost:9092
bin\windows\kafka-topics.bat --create --topic order.cancelled --bootstrap-server localhost:9092

# Linux/Mac
bin/kafka-topics.sh --create --topic order.created --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic order.confirmed --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic order.assigned --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic order.cancelled --bootstrap-server localhost:9092
```

### Étape 4: Compiler les Services

```bash
# Compiler config-server
cd config-server
mvn clean install -DskipTests
cd ..

# Compiler order-service
cd order-service
mvn clean install -DskipTests
cd ..
```

---

## ▶️ Démarrage du Système

### Ordre de Démarrage (IMPORTANT!)

Les services doivent être démarrés dans cet ordre:

#### 1. Config Server (PREMIER)

```bash
cd config-server
mvn spring-boot:run
```

**Vérification:**
```bash
curl http://localhost:8888/order-service/default
# Doit retourner la configuration JSON
```

#### 2. Eureka Server (DEUXIÈME)

```bash
cd eureka-server
mvn spring-boot:run
```

**Vérification:**
- Ouvrir http://localhost:8761
- Vous devriez voir le dashboard Eureka

#### 3. Order Service (TROISIÈME)

```bash
cd order-service
mvn spring-boot:run
```

**Vérification:**
```bash
curl http://localhost:8084/actuator/health
# Doit retourner: {"status":"UP"}
```

**Vérifier l'enregistrement Eureka:**
- Ouvrir http://localhost:8761
- Vous devriez voir "ORDER-SERVICE" dans la liste

### Démarrage avec Profils

```bash
# Profil de développement
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Profil de test
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

---

## 🧪 Tests des Endpoints

### Configuration Initiale

Tous les exemples utilisent `curl`. Vous pouvez aussi utiliser Postman ou Insomnia.

### 1. Créer une Commande

```bash
curl -X POST http://localhost:8084/orders \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": 1,
    "parcelId": 5,
    "transportType": "STANDARD",
    "totalPrice": 25.50,
    "scheduledAt": "2026-04-15T10:00:00"
  }'
```

**Réponse attendue (201 Created):**
```json
{
  "id": 1,
  "clientId": 1,
  "parcelId": 5,
  "driverId": null,
  "status": "PENDING",
  "transportType": "STANDARD",
  "totalPrice": 25.50,
  "scheduledAt": "2026-04-15T10:00:00",
  "deliveredAt": null
}
```

### 2. Récupérer une Commande

```bash
curl http://localhost:8084/orders/1
```

### 3. Créer un Paiement

```bash
curl -X POST http://localhost:8084/orders/1/payment \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 25.50,
    "method": "CARD"
  }'
```

**Réponse attendue (201 Created):**
```json
{
  "id": 1,
  "orderId": 1,
  "amount": 25.50,
  "method": "CARD",
  "status": "PENDING",
  "paidAt": null
}
```

### 4. Confirmer une Commande

```bash
curl -X POST http://localhost:8084/orders/1/confirm
```

**Note:** Nécessite un paiement avec status=COMPLETED

### 5. Assigner un Chauffeur

```bash
curl -X PUT http://localhost:8084/orders/1/assign-driver \
  -H "Content-Type: application/json" \
  -d '{
    "driverId": 3
  }'
```

### 6. Ajouter un Événement de Suivi

```bash
curl -X POST http://localhost:8084/orders/1/tracking \
  -H "Content-Type: application/json" \
  -d '{
    "city": "Dakar",
    "lat": 14.6928,
    "lng": -17.4467,
    "status": "IN_TRANSIT",
    "timestamp": "2026-04-15T11:30:00"
  }'
```

### 7. Récupérer l'Historique de Suivi

```bash
curl http://localhost:8084/orders/1/tracking-history
```

### 8. Récupérer les Commandes d'un Client

```bash
curl "http://localhost:8084/orders/client/1?page=0&size=10&sort=scheduledAt,desc"
```

### 9. Annuler une Commande

```bash
curl -X POST http://localhost:8084/orders/1/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Client requested cancellation"
  }'
```

### 10. Mettre à Jour le Statut

```bash
curl -X PUT http://localhost:8084/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_TRANSIT"
  }'
```

---

## 🔗 Communication Inter-Services

### Feign Clients Implémentés

Order Service communique avec d'autres services via Feign:

#### 1. ParcelServiceClient

```java
// Vérifier l'existence d'un colis
GET http://parcel-service/parcels/{id}

// Calculer le tarif
POST http://parcel-service/parcels/{id}/calculate-tariff
```

#### 2. DriverServiceClient

```java
// Récupérer un chauffeur
GET http://driver-service/drivers/{id}

// Vérifier la disponibilité
GET http://driver-service/drivers/{id}/availability

// Assigner un chauffeur
POST http://driver-service/drivers/assign
```

#### 3. PaymentServiceClient

```java
// Créer un paiement
POST http://payment-service/payments

// Confirmer un paiement
POST http://payment-service/payments/{id}/confirm
```

#### 4. GroupingServiceClient

```java
// Ajouter à un groupe
POST http://grouping-service/grouping/add
```

### Événements Kafka Publiés

Order Service publie les événements suivants:

| Événement | Topic | Quand |
|-----------|-------|-------|
| OrderCreatedEvent | order.created | Création de commande |
| OrderConfirmedEvent | order.confirmed | Confirmation de commande |
| OrderAssignedEvent | order.assigned | Assignation de chauffeur |
| OrderInTransitEvent | order.in-transit | Commande en transit |
| OrderDeliveredEvent | order.delivered | Livraison terminée |
| OrderCancelledEvent | order.cancelled | Annulation de commande |

### Tester les Événements Kafka

#### Consumer de test

```bash
# Windows
bin\windows\kafka-console-consumer.bat --topic order.created --from-beginning --bootstrap-server localhost:9092

# Linux/Mac
bin/kafka-console-consumer.sh --topic order.created --from-beginning --bootstrap-server localhost:9092
```

#### Vérifier tous les topics

```bash
# Windows
bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092

# Linux/Mac
bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

---

## 📊 Monitoring

### Health Checks

```bash
# Order Service
curl http://localhost:8084/actuator/health

# Config Server
curl http://localhost:8888/actuator/health

# Eureka Server
curl http://localhost:8761/actuator/health
```

### Informations du Service

```bash
curl http://localhost:8084/actuator/info
```

### Métriques

```bash
curl http://localhost:8084/actuator/metrics
```

### Métriques Spécifiques

```bash
# Utilisation mémoire
curl http://localhost:8084/actuator/metrics/jvm.memory.used

# Threads actifs
curl http://localhost:8084/actuator/metrics/jvm.threads.live

# Requêtes HTTP
curl http://localhost:8084/actuator/metrics/http.server.requests
```

---

## 🐛 Troubleshooting

### Problème: Config Server ne démarre pas

**Symptôme:** Erreur au démarrage

**Solution:**
```bash
# Vérifier le port 8888
netstat -ano | findstr :8888

# Si occupé, tuer le processus ou changer le port dans application.yml
```

### Problème: Order Service ne peut pas se connecter à PostgreSQL

**Symptôme:** `Connection refused` ou `Authentication failed`

**Solutions:**

1. Vérifier que PostgreSQL est démarré:
```bash
# Windows
sc query postgresql-x64-14

# Linux
sudo systemctl status postgresql
```

2. Vérifier les credentials dans `order-service.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/order_db
    username: orderuser
    password: orderpass
```

3. Tester la connexion manuellement:
```bash
psql -U orderuser -d order_db -h localhost
```

### Problème: Service ne s'enregistre pas dans Eureka

**Symptôme:** Service absent du dashboard Eureka

**Solutions:**

1. Vérifier qu'Eureka est démarré:
```bash
curl http://localhost:8761
```

2. Vérifier la configuration Eureka dans `order-service.yml`:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

3. Attendre 30 secondes (délai d'enregistrement)

### Problème: Kafka ne publie pas les événements

**Symptôme:** Aucun message dans les topics

**Solutions:**

1. Vérifier que Kafka est démarré:
```bash
# Windows
netstat -ano | findstr :9092

# Linux
netstat -tuln | grep 9092
```

2. Vérifier les topics:
```bash
bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

3. Vérifier les logs du service:
```bash
# Dans order-service
tail -f logs/order-service.log
```

### Problème: Feign Client timeout

**Symptôme:** `Read timed out` ou `Connection timeout`

**Solutions:**

1. Augmenter les timeouts dans `order-service.yml`:
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 10000
        readTimeout: 10000
```

2. Vérifier que le service cible est démarré

3. Vérifier Eureka pour la résolution de noms

### Logs Utiles

```bash
# Activer les logs DEBUG
# Dans application.yml
logging:
  level:
    sn.edu.ept.order: DEBUG
    org.springframework.kafka: DEBUG
    feign: DEBUG
```

---

## 📝 Scripts de Test Complets

### Script PowerShell (Windows)

Créer `test-order-service.ps1`:

```powershell
# Test Order Service Endpoints

$baseUrl = "http://localhost:8084"

Write-Host "=== Test 1: Health Check ===" -ForegroundColor Green
curl "$baseUrl/actuator/health"

Write-Host "`n=== Test 2: Create Order ===" -ForegroundColor Green
$createOrder = @{
    clientId = 1
    parcelId = 5
    transportType = "STANDARD"
    totalPrice = 25.50
    scheduledAt = "2026-04-15T10:00:00"
} | ConvertTo-Json

$order = Invoke-RestMethod -Uri "$baseUrl/orders" -Method Post -Body $createOrder -ContentType "application/json"
$orderId = $order.id
Write-Host "Order created with ID: $orderId"

Write-Host "`n=== Test 3: Get Order ===" -ForegroundColor Green
curl "$baseUrl/orders/$orderId"

Write-Host "`n=== Test 4: Create Payment ===" -ForegroundColor Green
$createPayment = @{
    amount = 25.50
    method = "CARD"
} | ConvertTo-Json

curl "$baseUrl/orders/$orderId/payment" -Method Post -Body $createPayment -ContentType "application/json"

Write-Host "`n=== Test 5: Add Tracking Event ===" -ForegroundColor Green
$trackingEvent = @{
    city = "Dakar"
    lat = 14.6928
    lng = -17.4467
    status = "IN_TRANSIT"
    timestamp = "2026-04-15T11:30:00"
} | ConvertTo-Json

curl "$baseUrl/orders/$orderId/tracking" -Method Post -Body $trackingEvent -ContentType "application/json"

Write-Host "`n=== Test 6: Get Tracking History ===" -ForegroundColor Green
curl "$baseUrl/orders/$orderId/tracking-history"

Write-Host "`n=== Tests completed ===" -ForegroundColor Green
```

Exécuter:
```powershell
.\test-order-service.ps1
```

### Script Bash (Linux/Mac)

Créer `test-order-service.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8084"

echo "=== Test 1: Health Check ==="
curl -s $BASE_URL/actuator/health | jq

echo -e "\n=== Test 2: Create Order ==="
ORDER_RESPONSE=$(curl -s -X POST $BASE_URL/orders \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": 1,
    "parcelId": 5,
    "transportType": "STANDARD",
    "totalPrice": 25.50,
    "scheduledAt": "2026-04-15T10:00:00"
  }')

ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.id')
echo "Order created with ID: $ORDER_ID"
echo $ORDER_RESPONSE | jq

echo -e "\n=== Test 3: Get Order ==="
curl -s $BASE_URL/orders/$ORDER_ID | jq

echo -e "\n=== Test 4: Create Payment ==="
curl -s -X POST $BASE_URL/orders/$ORDER_ID/payment \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 25.50,
    "method": "CARD"
  }' | jq

echo -e "\n=== Test 5: Add Tracking Event ==="
curl -s -X POST $BASE_URL/orders/$ORDER_ID/tracking \
  -H "Content-Type: application/json" \
  -d '{
    "city": "Dakar",
    "lat": 14.6928,
    "lng": -17.4467,
    "status": "IN_TRANSIT",
    "timestamp": "2026-04-15T11:30:00"
  }'

echo -e "\n=== Test 6: Get Tracking History ==="
curl -s $BASE_URL/orders/$ORDER_ID/tracking-history | jq

echo -e "\n=== Tests completed ==="
```

Exécuter:
```bash
chmod +x test-order-service.sh
./test-order-service.sh
```

---

## 📚 Documentation Additionnelle

- **API Documentation:** [order-service/API.md](order-service/API.md)
- **Requirements:** [.kiro/specs/complete-delivery-microservices-system/requirements.md](.kiro/specs/complete-delivery-microservices-system/requirements.md)
- **Design:** [.kiro/specs/complete-delivery-microservices-system/design.md](.kiro/specs/complete-delivery-microservices-system/design.md)
- **Tasks:** [.kiro/specs/complete-delivery-microservices-system/tasks.md](.kiro/specs/complete-delivery-microservices-system/tasks.md)

---

## 🎓 Prochaines Étapes

### Services à Implémenter

1. **Eureka Server** - Découverte de services
2. **API Gateway** - Point d'entrée unique avec sécurité
3. **Auth Service** - Authentification JWT
4. **User Service** - Gestion des utilisateurs
5. **Parcel Service** - Gestion des colis
6. **Driver Service** - Gestion des chauffeurs
7. **Payment Service** - Traitement des paiements
8. **Tracking Service** - Suivi temps réel avec Redis/WebSocket
9. **Grouping Service** - Optimisation des routes
10. **Notification Service** - Notifications multi-canal

### Améliorations Possibles

- [ ] Ajouter l'authentification JWT
- [ ] Implémenter le circuit breaker (Resilience4j)
- [ ] Ajouter le distributed tracing (Zipkin/Jaeger)
- [ ] Containeriser avec Docker
- [ ] Créer docker-compose.yml
- [ ] Ajouter les tests d'intégration
- [ ] Implémenter le rate limiting
- [ ] Ajouter Swagger/OpenAPI documentation

---

## 👥 Support

Pour toute question ou problème:
1. Consulter la section [Troubleshooting](#troubleshooting)
2. Vérifier les logs des services
3. Consulter la documentation API

---

## 📄 Licence

[Votre licence ici]

---

**Version:** 1.0.0  
**Dernière mise à jour:** 13 Avril 2026
