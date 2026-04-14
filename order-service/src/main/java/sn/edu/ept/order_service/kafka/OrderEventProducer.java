package sn.edu.ept.order_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import sn.edu.ept.order_service.dto.event.*;
import sn.edu.ept.order_service.entity.Order;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_CREATED_TOPIC = "order.created";
    private static final String ORDER_CONFIRMED_TOPIC = "order.confirmed";
    private static final String ORDER_ASSIGNED_TOPIC = "order.assigned";
    private static final String ORDER_IN_TRANSIT_TOPIC = "order.in-transit";
    private static final String ORDER_DELIVERED_TOPIC = "order.delivered";
    private static final String ORDER_CANCELLED_TOPIC = "order.cancelled";

    /**
     * Publier un événement de création de commande
     * @param order La commande créée
     */
    public void publishOrderCreated(Order order) {
        try {
            OrderCreatedEvent event = new OrderCreatedEvent(
                    order.getId(),
                    order.getClientId(),
                    order.getParcelId(),
                    order.getTotalPrice()
            );
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(ORDER_CREATED_TOPIC, order.getId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Événement order.created publié avec succès pour orderId={}", order.getId());
                } else {
                    log.error("Échec de publication de l'événement order.created pour orderId={}", order.getId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement order.created pour orderId={}", order.getId(), e);
        }
    }

    /**
     * Publier un événement de confirmation de commande
     * @param order La commande confirmée
     * @param paymentId L'identifiant du paiement
     */
    public void publishOrderConfirmed(Order order, Long paymentId) {
        try {
            OrderConfirmedEvent event = new OrderConfirmedEvent(
                    order.getId(),
                    paymentId
            );
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(ORDER_CONFIRMED_TOPIC, order.getId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Événement order.confirmed publié avec succès pour orderId={}", order.getId());
                } else {
                    log.error("Échec de publication de l'événement order.confirmed pour orderId={}", order.getId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement order.confirmed pour orderId={}", order.getId(), e);
        }
    }

    /**
     * Publier un événement d'assignation de chauffeur
     * @param order La commande avec chauffeur assigné
     */
    public void publishOrderAssigned(Order order) {
        try {
            OrderAssignedEvent event = new OrderAssignedEvent(
                    order.getId(),
                    order.getDriverId()
            );
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(ORDER_ASSIGNED_TOPIC, order.getId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Événement order.assigned publié avec succès pour orderId={}", order.getId());
                } else {
                    log.error("Échec de publication de l'événement order.assigned pour orderId={}", order.getId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement order.assigned pour orderId={}", order.getId(), e);
        }
    }

    /**
     * Publier un événement de livraison en cours
     * @param order La commande en transit
     */
    public void publishOrderInTransit(Order order) {
        try {
            OrderInTransitEvent event = new OrderInTransitEvent(
                    order.getId(),
                    order.getDriverId()
            );
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(ORDER_IN_TRANSIT_TOPIC, order.getId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Événement order.in-transit publié avec succès pour orderId={}", order.getId());
                } else {
                    log.error("Échec de publication de l'événement order.in-transit pour orderId={}", order.getId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement order.in-transit pour orderId={}", order.getId(), e);
        }
    }

    /**
     * Publier un événement de livraison terminée
     * @param order La commande livrée
     */
    public void publishOrderDelivered(Order order) {
        try {
            OrderDeliveredEvent event = new OrderDeliveredEvent(
                    order.getId(),
                    order.getDriverId(),
                    order.getDeliveredAt() != null ? order.getDeliveredAt() : LocalDateTime.now()
            );
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(ORDER_DELIVERED_TOPIC, order.getId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Événement order.delivered publié avec succès pour orderId={}", order.getId());
                } else {
                    log.error("Échec de publication de l'événement order.delivered pour orderId={}", order.getId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement order.delivered pour orderId={}", order.getId(), e);
        }
    }

    /**
     * Publier un événement d'annulation de commande
     * @param order La commande annulée
     * @param reason La raison de l'annulation
     */
    public void publishOrderCancelled(Order order, String reason) {
        try {
            OrderCancelledEvent event = new OrderCancelledEvent(
                    order.getId(),
                    reason
            );
            
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(ORDER_CANCELLED_TOPIC, order.getId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Événement order.cancelled publié avec succès pour orderId={}", order.getId());
                } else {
                    log.error("Échec de publication de l'événement order.cancelled pour orderId={}", order.getId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement order.cancelled pour orderId={}", order.getId(), e);
        }
    }
}
