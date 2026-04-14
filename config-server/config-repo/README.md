# Configuration Repository

Ce répertoire contient les fichiers de configuration pour les microservices gérés par le Spring Cloud Config Server.

## Structure des Fichiers de Configuration

Le Config Server utilise une structure de fichiers hiérarchique pour gérer les configurations:

```
config-repo/
├── application.yml              # Configuration globale (tous les services)
├── {service-name}.yml           # Configuration spécifique à un service
└── {service-name}-{profile}.yml # Configuration spécifique à un service et un profil
```

### Types de Fichiers

#### 1. Configuration Globale: `application.yml`

Contient les propriétés partagées par tous les microservices.

**Exemple:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info

logging:
  level:
    root: INFO
```

**Exigence: 5.1** - Toutes les propriétés définies dans `application.yml` sont héritées par tous les services.

#### 2. Configuration Spécifique au Service: `{service-name}.yml`

Contient les propriétés spécifiques à un microservice particulier.

**Exemple: `order-service.yml`**
```yaml
server:
  port: 8081

spring:
  application:
    name: order-service
  datasource:
    url: jdbc:postgresql://localhost:5432/orders
    username: orderuser
    password: orderpass

order:
  processing:
    timeout: 30000
    max-retries: 3
```

**Exigence: 6.1** - Les propriétés du service remplacent les propriétés globales en cas de conflit.

#### 3. Configuration Spécifique au Profil: `{service-name}-{profile}.yml`

Contient les propriétés spécifiques à un environnement (dev, prod, test).

**Exemple: `order-service-dev.yml`**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/orders_dev
    username: devuser
    password: devpass

logging:
  level:
    sn.edu.ept: DEBUG

order:
  processing:
    timeout: 60000
```

**Exigence: 7.1** - Les propriétés du profil ont la priorité la plus élevée.

## Conventions de Nommage

### Noms de Fichiers

1. **Format général**: `{application}-{profile}.{extension}`
   - `{application}`: Nom du microservice (ex: order-service, user-service)
   - `{profile}`: Environnement cible (dev, prod, test) - optionnel
   - `{extension}`: yml ou properties

2. **Règles de nommage**:
   - Utiliser des minuscules
   - Séparer les mots par des tirets (kebab-case)
   - Éviter les espaces et caractères spéciaux
   - Le nom doit correspondre à `spring.application.name` du microservice

3. **Exemples valides**:
   - `application.yml` ✓
   - `order-service.yml` ✓
   - `order-service-dev.yml` ✓
   - `user-management-service-prod.yml` ✓

4. **Exemples invalides**:
   - `OrderService.yml` ✗ (majuscules)
   - `order_service.yml` ✗ (underscore au lieu de tiret)
   - `order service.yml` ✗ (espace)

### Noms de Propriétés

1. **Format**: Utiliser la notation kebab-case pour les clés YAML
   ```yaml
   # Correct
   spring:
     application:
       name: order-service
   
   # Incorrect
   spring:
     applicationName: order-service
   ```

2. **Hiérarchie**: Organiser les propriétés de manière logique
   ```yaml
   order:
     processing:
       timeout: 30000
       max-retries: 3
     notification:
       enabled: true
       email: admin@example.com
   ```

## Ordre de Priorité des Configurations

Le Config Server fusionne les configurations selon l'ordre de priorité suivant (du plus faible au plus fort):

1. **Configuration globale** (`application.yml`)
2. **Configuration du service** (`{service-name}.yml`)
3. **Configuration du profil** (`{service-name}-{profile}.yml`)

**Exigence: 7.3** - Une propriété définie dans un fichier de priorité supérieure remplace celle du fichier de priorité inférieure.

### Exemple de Fusion

Fichiers:
```yaml
# application.yml
logging:
  level:
    root: INFO
server:
  port: 8080

# order-service.yml
server:
  port: 8081
order:
  timeout: 30000

# order-service-dev.yml
logging:
  level:
    root: DEBUG
order:
  timeout: 60000
```

Configuration finale pour `order-service` avec profil `dev`:
```yaml
logging:
  level:
    root: DEBUG        # De order-service-dev.yml
server:
  port: 8081          # De order-service.yml
order:
  timeout: 60000      # De order-service-dev.yml
```

## Exemples d'Utilisation

### 1. Accéder à la Configuration d'un Service

**Endpoint**: `GET http://localhost:8888/{application}/{profile}`

**Exemples**:

```bash
# Configuration par défaut du service order-service
curl http://localhost:8888/order-service/default

# Configuration du service order-service pour l'environnement dev
curl http://localhost:8888/order-service/dev

# Configuration globale
curl http://localhost:8888/application/default
```

**Exigence: 8.1, 8.2** - Le serveur retourne la configuration fusionnée au format JSON.

### 2. Réponse JSON

```json
{
  "name": "order-service",
  "profiles": ["dev"],
  "label": null,
  "version": null,
  "state": null,
  "propertySources": [
    {
      "name": "file:./config-repo/order-service-dev.yml",
      "source": {
        "spring.datasource.url": "jdbc:postgresql://localhost:5432/orders_dev",
        "logging.level.sn.edu.ept": "DEBUG"
      }
    },
    {
      "name": "file:./config-repo/order-service.yml",
      "source": {
        "server.port": 8081,
        "spring.application.name": "order-service"
      }
    },
    {
      "name": "file:./config-repo/application.yml",
      "source": {
        "management.endpoints.web.exposure.include": "health,info"
      }
    }
  ]
}
```

### 3. Configurer un Microservice Client

Dans le microservice client, ajouter la dépendance:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

Configurer `application.yml` du client:

```yaml
spring:
  application:
    name: order-service
  config:
    import: "configserver:http://localhost:8888"
  profiles:
    active: dev
```

Le microservice récupérera automatiquement sa configuration au démarrage.

## Gestion des Erreurs

### Fichiers YAML Invalides

**Exigence: 9.1, 9.2, 9.3** - Le serveur gère les erreurs de parsing YAML:

- Les erreurs de syntaxe sont loguées avec le chemin du fichier et le numéro de ligne
- Les fichiers malformés sont ignorés
- Les autres fichiers valides continuent d'être chargés

**Exemple de log d'erreur**:
```
ERROR: Failed to parse YAML file: ./config-repo/order-service.yml
Line 5: mapping values are not allowed here
```

### Fichier de Configuration Manquant

**Exigence: 6.3** - Si un fichier `{service-name}.yml` n'existe pas, le serveur retourne uniquement la configuration globale sans erreur.

## Bonnes Pratiques

1. **Sécurité**:
   - Ne jamais commiter de mots de passe en clair
   - Utiliser des variables d'environnement ou un vault pour les secrets
   - Restreindre les permissions du répertoire config-repo

2. **Organisation**:
   - Grouper les propriétés par domaine fonctionnel
   - Utiliser des commentaires pour documenter les propriétés complexes
   - Maintenir une structure cohérente entre les fichiers

3. **Validation**:
   - Valider la syntaxe YAML avant de déployer
   - Tester les configurations dans un environnement de développement
   - Utiliser des outils de validation YAML (yamllint)

4. **Versioning**:
   - Versionner les fichiers de configuration dans Git
   - Utiliser des branches pour les environnements (dev, staging, prod)
   - Documenter les changements dans les commits

## Références

- [Spring Cloud Config Documentation](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/)
- [YAML Syntax](https://yaml.org/spec/1.2/spec.html)
- Exigences: 5.1, 6.1, 7.1 (voir requirements.md)
