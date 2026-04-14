package sn.edu.ept.order_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Application principale du service de gestion des commandes.
 * 
 * Ce service orchestre les commandes de livraison en coordonnant:
 * - La validation des colis (parcel-service)
 * - L'assignation des chauffeurs (driver-service)
 * - Le traitement des paiements (payment-service)
 * - Le suivi des livraisons (tracking-service)
 * - Les notifications (notification-service via Kafka)
 * 
 * Annotations:
 * - @SpringBootApplication: Configuration Spring Boot standard
 * - @EnableFeignClients: Active les clients Feign pour communication REST inter-services
 * - @EnableEurekaClient: Enregistre le service auprès d'Eureka pour découverte dynamique
 * 
 * Port par défaut: 8084
 * 
 * @author Order Service Team
 * @version 1.0
 */
@SpringBootApplication
@EnableFeignClients
@EnableEurekaClient
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
