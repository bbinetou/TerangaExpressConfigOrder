# Guide de Test Complet - Config Service Integration

## 📋 Vue d'ensemble

Ce guide fournit des instructions complètes pour tester l'intégration entre config-server, order-service, parcel-service et user-service.

## 🧪 Types de Tests

### 1. Tests Unitaires
Tests des composants individuels en isolation.

### 2. Tests d'Intégration
Tests de la communication entre services avec WireMock.

### 3. Tests End-to-End
Tests du système complet avec tous les services démarrés.

---

## ⚡ Tests Rapides

### Exécuter Tous les Tests

```bash
cd order-service
./mvnw clean test
```

**Résultat attendu:**
- ✅ 43+ tests passent
- ⏱️ Durée: ~40-50 secondes

### Exécuter Uniquement les Tests d'Intégration

```bash
./mvnw test -Dtest='*IntegrationTest'
```

**Tests inclus:**
- `ParcelServiceIntegrationTest` (7 tests)
- `UserServiceIntegrationTest` (9 tests)
- `EurekaServiceDiscoveryIntegrationTest` (6 tests)

---

## 📊 Détails des Tests d'Intégration

### ParcelServiceIntegrationTest

**Objectif:** Tester la communication order-service → parcel-service

**Tests:**
1. ✅ `testGetParcel_Success` - Récupération réussie d'un colis
2. ✅ `testCalculateTariff_Success` - Calcul de tarif réussi
3. ✅ `testGetParcel_NotFound_ThrowsResourceNotFoundException` - Gestion 404
4. ✅ `testGetParcel_InternalServerError_ThrowsServiceUnavailableException` - Gestion 500
5. ✅ `testGetParcel_ServiceUnavailable_ThrowsServiceUnavailableException` - Gestion 503
6. ✅ `testCalculateTariff_NotFound_ThrowsResourceNotFoundException` - Gestion 404 tarif

**Commande:**
```bash
./mvnw test -Dtest=ParcelServiceIntegrationTest
```

### UserServiceIntegrationTest

**Objectif:** Tester la communication order-service → user-service

**Tests:**
1. ✅ `testGetUser_Success` - Récupération réussie d'un utilisateur
2. ✅ `testValidateUserExists_Success_UserExists` - Validation utilisateur existant
3. ✅ `testValidateUserExists_Success_UserDoesNotExist` - Validation utilisateur inexistant
4. ✅ `testGetUser_NotFound_ThrowsResourceNotFoundException` - Gestion 404
5. ✅ `testGetUser_InternalServerError_ThrowsServiceUnavailableException` - Gestion 500
6. ✅ `testGetUser_ServiceUnavailable_ThrowsServiceUnavailableException` - Gestion 503
7. ✅ `testValidateUserExists_NotFound_ThrowsResourceNotFoundException` - Gestion 404 validation
8. ✅ `testValidateUserExists_InternalServerError_ThrowsServiceUnavailableException` - Gestion 500 validation

**Commande:**
```bash
./mvnw test -Dtest=UserServiceIntegrationTest
```

### EurekaServiceDiscoveryIntegrationTest

**Objectif:** Tester la résolution de services via Eureka

**Tests:**
1. ✅ `testFeignClientResolvesParcelServiceViaConfiguration` - Résolution parcel-service
2. ✅ `testFeignClientResolvesUserServiceViaConfiguration` - Résolution user-service
3. ✅ `testServiceUnavailable_WhenServiceNotRunning` - Service indisponible
4. ✅ `testMultipleServicesCanBeResolvedSimultaneously` - Résolution multiple
5. ✅ `testEurekaClientDisabledInTestProfile` - Eureka désactivé en test
6. ✅ `testFeignClientUsesConfiguredTimeouts` - Timeouts configurés

**Commande:**
```bash
./mvnw test -Dtest=EurekaServiceDiscoveryIntegrationTest
```

---

## 🔧 Tests de Configuration

### Vérifier Config Server

```bash
cd config-server
./mvnw test
```

**Tests inclus:**
- `ConfigServerApplicationTests` - Test de démarrage
- `ConfigServerIntegrationTest` - Test des endpoints de configuration
- `ConfigFallbackIntegrationTest` - Test du fallback
- `ConfigurationLoadingPropertiesTest` - Test du chargement des propriétés

**Résultat attendu:**
- ✅ 4 tests passent
- ⏱️ Durée: ~15-20 secondes

---

## 🌐 Tests End-to-End Manuels

### Prérequis

1. **Démarrer PostgreSQL**
```bash
# Vérifier que PostgreSQL est démarré
psql -U postgres -c "SELECT version();"
```

2. **Démarrer Config Server**
```bash
cd config-server
./mvnw spring-boot:run
```

Vérifier: http://localhost:8888/actuator/health

3. **Démarrer Order Service**
```bash
cd order-service
./mvnw spring-boot:run
```

Vérifier: http://localhost:8084/actuator/health

### Test 1: Créer une Commande

```bash
curl -X POST http://localhost:8084/orders \ -H "Content-Type: application/json" \ -d '{"clientId": 1, "parcelId": 5, "transportType": "STANDARD", "totalPrice": 25.50, "scheduledAt": "2026-04-20T10:00:00"}'
```

**Résultat attendu:**
```json
{
  "id": 1,
  "clientId": 1,
  "parcelId": 5,
  "status": "PENDING",
  "transportType": "STANDARD",
  "totalPrice": 25.50
}
```

### Test 2: Récupérer une Commande

```bash
curl http://localhost:8084/orders/1
```

### Test 3: Vérifier la Configuration

```bash
# Configuration order-service
curl http://localhost:8888/order-service/default

# Configuration parcel-service
curl http://localhost:8888/parcel-service/default

# Configuration user-service
curl http://localhost:8888/user-service/default
```

---

## 📈 Résultats Attendus

### Tests Unitaires + Intégration

```
Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
```

**Détails:**
- ✅ 22 tests d'intégration (ParcelService, UserService, Eureka)
- ✅ 7 tests Kafka (OrderEventProducer)
- ✅ 6 tests service (OrderServiceConfirmOrder)
- ✅ 9 tests service (OrderServiceCreatePayment)
- ✅ 1 test application (OrderServiceApplicationTests)

### Couverture de Code

**Objectif:** 80% minimum

**Zones couvertes:**
- ✅ Feign Clients (ParcelServiceClient, UserServiceClient)
- ✅ Error Decoder (CustomFeignErrorDecoder)
- ✅ Exception Handlers (GlobalExceptionHandler)
- ✅ DTOs (UserResponse, UserExistsResponse)
- ✅ Kafka Producers (OrderEventProducer)
- ✅ Services (OrderService)

---

## 🐛 Troubleshooting

### Problème: Tests échouent avec "Connection refused"

**Cause:** WireMock ne démarre pas correctement

**Solution:**
```bash
# Vérifier les ports
netstat -ano | findstr :8082
netstat -ano | findstr :8083

# Si occupés, tuer les processus
taskkill /PID <PID> /F
```

### Problème: Tests échouent avec "KafkaTemplate not found"

**Cause:** Configuration Kafka manquante

**Solution:**
- Vérifier que `TestConfig.java` existe
- Vérifier que `@Import(TestConfig.class)` est présent sur les tests

### Problème: Tests lents (>2 minutes)

**Cause:** Timeouts trop longs

**Solution:**
```yaml
# Dans application-test.yml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
```

---

## 📝 Checklist de Validation

Avant de considérer l'intégration comme complète:

- [ ] ✅ Tous les tests unitaires passent
- [ ] ✅ Tous les tests d'intégration passent
- [ ] ✅ Config Server démarre sans erreur
- [ ] ✅ Order Service démarre sans erreur
- [ ] ✅ Order Service peut récupérer sa configuration depuis Config Server
- [ ] ✅ Feign Clients peuvent communiquer avec les services simulés
- [ ] ✅ Les erreurs sont correctement gérées (404, 500, 503)
- [ ] ✅ Les événements Kafka sont publiés correctement
- [ ] ✅ La documentation est à jour

---

## 🚀 Commandes Rapides

```bash
# Tests complets
./mvnw clean test

# Tests d'intégration uniquement
./mvnw test -Dtest='*IntegrationTest'

# Test spécifique
./mvnw test -Dtest=ParcelServiceIntegrationTest#testGetParcel_Success

# Tests avec logs détaillés
./mvnw test -X

# Tests sans compilation
./mvnw surefire:test

# Générer rapport de couverture
./mvnw jacoco:report
```

---

## 📚 Ressources

- **Spec Requirements:** `.kiro/specs/config-service-integration/requirements.md`
- **Spec Design:** `.kiro/specs/config-service-integration/design.md`
- **Spec Tasks:** `.kiro/specs/config-service-integration/tasks.md`
- **API Documentation:** `order-service/API.md`

---

**Version:** 1.0.0  
**Dernière mise à jour:** 17 Avril 2026
