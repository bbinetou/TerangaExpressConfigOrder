package sn.edu.ept.config_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Application principale du Spring Cloud Config Server.
 * 
 * Ce serveur centralise la gestion des configurations pour les microservices.
 * Il utilise le profil "native" pour lire les fichiers de configuration depuis
 * le répertoire local config-repo et expose ces configurations via des endpoints REST.
 * 
 * Configuration du serveur:
 * - Port d'écoute: 8888
 * - Profil actif: native
 * - Répertoire de configuration: ./config-repo
 * 
 * Endpoints exposés:
 * - /{application}/{profile}: Récupère la configuration pour un service et un profil donnés
 * - /actuator/health: Endpoint de santé pour le monitoring
 * - /actuator/info: Informations sur l'application
 * 
 * @see EnableConfigServer
 */
@SpringBootApplication
/**
 * @EnableConfigServer active la fonctionnalité Config Server de Spring Cloud.
 * 
 * Cette annotation:
 * - Active les endpoints REST de configuration (/{application}/{profile})
 * - Configure le serveur pour lire les configurations depuis la source définie (native profile)
 * - Permet la fusion des configurations (global, service-specific, profile-specific)
 * - Gère automatiquement le parsing YAML et la conversion en JSON
 * 
 * Exigences satisfaites: 1.1, 1.2
 */
@EnableConfigServer
public class ConfigServerApplication {

	/**
	 * Point d'entrée de l'application Config Server.
	 * 
	 * Démarre le serveur Spring Boot avec la configuration définie dans application.yml.
	 * Le serveur sera accessible sur le port 8888 et exposera les configurations
	 * des microservices via des endpoints REST.
	 * 
	 * @param args Arguments de ligne de commande (non utilisés)
	 */
	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}

}
