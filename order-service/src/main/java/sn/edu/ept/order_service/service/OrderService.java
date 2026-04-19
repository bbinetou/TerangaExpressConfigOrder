package sn.edu.ept.order_service.service;

import sn.edu.ept.order_service.client.DriverServiceClient;
import sn.edu.ept.order_service.client.ParcelServiceClient;
import sn.edu.ept.order_service.client.PaymentServiceClient;
import sn.edu.ept.order_service.exception.DriverNotAvailableException;
import sn.edu.ept.order_service.exception.InvalidParcelException;
import sn.edu.ept.order_service.exception.OrderNotFoundException;
import sn.edu.ept.order_service.kafka.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.order_service.dto.*;
import sn.edu.ept.order_service.entity.Order;
import sn.edu.ept.order_service.entity.OrderStatus;
import sn.edu.ept.order_service.entity.Payment;
import sn.edu.ept.order_service.entity.TrackingEvent;
import sn.edu.ept.order_service.repository.OrderRepository;
import sn.edu.ept.order_service.repository.PaymentRepository;
import sn.edu.ept.order_service.repository.TrackingEventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service principal pour la gestion des commandes de livraison.
 * 
 * Ce service orchestre les opérations suivantes:
 * - Création et gestion du cycle de vie des commandes
 * - Validation des colis via parcel-service
 * - Assignation des chauffeurs via driver-service
 * - Gestion des paiements
 * - Suivi des événements de livraison
 * - Publication d'événements Kafka pour communication asynchrone
 * 
 * @author Order Service Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class OrderService {
    
    // Repositories pour accès aux données
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final TrackingEventRepository trackingEventRepository;
    
    // Kafka producer pour événements asynchrones
    private final OrderEventProducer orderEventProducer;
    
    // Feign clients pour communication inter-services
    private final ParcelServiceClient parcelServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final DriverServiceClient driverServiceClient;
    
    /**
     * Crée une nouvelle commande de livraison.
     * 
     * Processus:
     * 1. Valide que le colis existe via parcel-service
     * 2. Vérifie que le colis a le statut CREATED
     * 3. Calcule le tarif via parcel-service
     * 4. Crée la commande avec statut PENDING
     * 5. Publie l'événement order.created sur Kafka
     * 
     * @param request les détails de la commande à créer
     * @return OrderResponse la commande créée
     * @throws InvalidParcelException si le colis n'existe pas ou n'a pas le bon statut
     * 
     * Exigences: 4.1, 4.2, 17.4
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Étape 1: Valider que le colis existe
        ParcelResponse parcel;
        try {
            parcel = parcelServiceClient.getParcel(request.getParcelId());
        } catch (Exception e) {
            throw new InvalidParcelException(request.getParcelId(), "Parcel not found or service unavailable");
        }
        
        // Étape 2: Vérifier que le colis a le statut CREATED (Exigence 4.1)
        if (!"EN_ATTENTE".equals(parcel.getStatus())) {
            throw new InvalidParcelException(
                request.getParcelId(), 
                String.format("Parcel status must be CREATED, but was %s", parcel.getStatus())
            );
        }
        
        // Étape 3: Calculer le tarif depuis parcel-service (Exigence 17.4)
        TariffResponse tariff;
        try {
            tariff = parcelServiceClient.calculateTariff(request.getParcelId());
        } catch (Exception e) {
            throw new InvalidParcelException(request.getParcelId(), "Failed to calculate tariff");
        }
        
        // Étape 4: Créer la commande avec le tarif calculé
        Order order = new Order();
        order.setClientId(request.getClientId());
        order.setParcelId(request.getParcelId());
        order.setTransportType(request.getTransportType());
        order.setTotalPrice(tariff.getTariff()); // Utiliser le tarif calculé (Exigence 17.4)
        order.setScheduledAt(request.getScheduledAt());
        order.setStatus(OrderStatus.PENDING); // Statut initial PENDING (Exigence 4.2)
        
        Order savedOrder = orderRepository.save(order);
        
        // Étape 5: Publier l'événement order.created sur Kafka
        orderEventProducer.publishOrderCreated(savedOrder);
        
        return mapToOrderResponse(savedOrder);
    }
    
    /**
     * Récupère une commande par son ID.
     * 
     * @param id l'identifiant de la commande
     * @return OrderResponse la commande trouvée
     * @throws OrderNotFoundException si la commande n'existe pas
     */
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
        return mapToOrderResponse(order);
    }
    
    /**
     * Met à jour le statut d'une commande.
     * Si le statut est DELIVERED, enregistre également la date de livraison.
     * 
     * @param id l'identifiant de la commande
     * @param status le nouveau statut
     * @return OrderResponse la commande mise à jour
     * @throws OrderNotFoundException si la commande n'existe pas
     * 
     * Exigence: 17.5 (deliveredAt doit être non null si status=DELIVERED)
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
        
        order.setStatus(status);
        
        // Si livraison terminée, enregistrer le timestamp (Exigence 17.5)
        if (status == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        
        Order updatedOrder = orderRepository.save(order);
        return mapToOrderResponse(updatedOrder);
    }
    
    /**
     * Assigne un chauffeur à une commande.
     * 
     * Processus:
     * 1. Vérifie que le chauffeur est disponible via driver-service
     * 2. Appelle driver-service pour marquer le chauffeur comme indisponible
     * 3. Met à jour la commande avec l'ID du chauffeur et statut ASSIGNED
     * 4. Publie l'événement order.assigned sur Kafka
     * 
     * @param orderId l'identifiant de la commande
     * @param driverId l'identifiant du chauffeur
     * @return OrderResponse la commande mise à jour
     * @throws OrderNotFoundException si la commande n'existe pas
     * @throws DriverNotAvailableException si le chauffeur n'est pas disponible
     * 
     * Exigences: 4.4, 4.5, 17.2
     */
    @Transactional
    public OrderResponse assignDriver(Long orderId, Long driverId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // Étape 1: Vérifier la disponibilité du chauffeur (Exigence 4.4)
        DriverAvailabilityResponse availability;
        try {
            availability = driverServiceClient.checkAvailability(driverId);
        } catch (Exception e) {
            throw new DriverNotAvailableException(driverId, "Failed to check driver availability: " + e.getMessage());
        }
        
        // Vérifier que le chauffeur est disponible
        if (availability.getAvailable() == null || !availability.getAvailable()) {
            throw new DriverNotAvailableException(driverId);
        }
        
        // Étape 2: Assigner le chauffeur via driver-service (met à jour available=false)
        try {
            AssignDriverRequest assignRequest = new AssignDriverRequest(driverId);
            driverServiceClient.assignDriver(assignRequest);
        } catch (Exception e) {
            throw new DriverNotAvailableException(driverId, "Failed to assign driver: " + e.getMessage());
        }
        
        // Étape 3: Mettre à jour la commande (Exigence 4.5, 17.2)
        order.setDriverId(driverId);
        order.setStatus(OrderStatus.ASSIGNED);
        
        Order updatedOrder = orderRepository.save(order);
        
        // Étape 4: Publier l'événement order.assigned
        orderEventProducer.publishOrderAssigned(updatedOrder);
        
        return mapToOrderResponse(updatedOrder);
    }
    
    /**
     * Ajoute un événement de suivi à une commande.
     * Enregistre la position géographique et le statut à un moment donné.
     * 
     * @param orderId l'identifiant de la commande
     * @param request les détails de l'événement de suivi
     * @throws OrderNotFoundException si la commande n'existe pas
     * @throws IllegalArgumentException si des champs requis sont manquants
     * 
     * Exigence: 4.8 (événements doivent contenir timestamp, lat, lng, city)
     */
    @Transactional
    public void addTrackingEvent(Long orderId, CreateTrackingEventRequest request) {
        // Vérifier que la commande existe
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException(orderId);
        }
        
        // Valider que tous les champs requis sont présents (Exigence 4.8)
        if (request.getCity() == null || request.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }
        if (request.getLat() == null) {
            throw new IllegalArgumentException("Latitude is required");
        }
        if (request.getLng() == null) {
            throw new IllegalArgumentException("Longitude is required");
        }
        
        // Créer l'événement de suivi
        TrackingEvent trackingEvent = new TrackingEvent();
        trackingEvent.setOrderId(orderId);
        trackingEvent.setCity(request.getCity());
        trackingEvent.setLat(request.getLat());
        trackingEvent.setLng(request.getLng());
        trackingEvent.setStatus(request.getStatus());
        
        // Utiliser le timestamp fourni ou l'heure actuelle
        trackingEvent.setTimestamp(
            request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now()
        );
        
        // Sauvegarder dans la base de données
        trackingEventRepository.save(trackingEvent);
    }
    
    /**
     * Crée un paiement pour une commande.
     * 
     * Validations:
     * - Le montant doit correspondre au totalPrice de la commande
     * - Aucun paiement complété ne doit déjà exister pour cette commande
     * 
     * @param orderId l'identifiant de la commande
     * @param request les détails du paiement
     * @return PaymentResponse le paiement créé avec statut PENDING
     * @throws OrderNotFoundException si la commande n'existe pas
     * @throws IllegalArgumentException si le montant ne correspond pas
     * @throws IllegalStateException si un paiement complété existe déjà
     * 
     * Exigences: 6.1, 6.2, 17.1
     */
    @Transactional
    public PaymentResponse createPayment(Long orderId, CreatePaymentRequest request) {
        // Récupérer la commande pour obtenir totalPrice (Exigence 6.2)
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // Valider que le montant correspond au totalPrice (Exigence 6.2)
        if (!request.getAmount().equals(order.getTotalPrice())) {
            throw new IllegalArgumentException(
                String.format("Payment amount %.2f does not match order totalPrice %.2f", 
                    request.getAmount(), order.getTotalPrice())
            );
        }
        
        // Vérifier qu'aucun paiement complété n'existe déjà (Exigence 17.1)
        List<Payment> existingPayments = paymentRepository.findByOrderId(orderId);
        boolean hasCompletedPayment = existingPayments.stream()
            .anyMatch(p -> "COMPLETED".equals(p.getStatus()));
        
        if (hasCompletedPayment) {
            throw new IllegalStateException(
                String.format("Order %d already has a completed payment", orderId)
            );
        }
        
        // Créer le paiement avec statut PENDING (Exigence 6.1)
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setStatus("PENDING");
        payment.setPaidAt(LocalDateTime.now());
        
        // Sauvegarder dans la base de données
        Payment savedPayment = paymentRepository.save(payment);
        return mapToPaymentResponse(savedPayment);
    }
    
    /**
     * Récupère les commandes d'un client avec pagination.
     * 
     * @param clientId l'identifiant du client
     * @param pageable paramètres de pagination (page, size, sort)
     * @return Page<OrderResponse> page de commandes
     * 
     * Exigence: 2.3
     */
    public Page<OrderResponse> getOrdersByClient(Long clientId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByClientId(clientId, pageable);
        return orders.map(this::mapToOrderResponse);
    }
    
    /**
     * Confirme une commande après que le paiement soit complété.
     * 
     * Précondition: Un paiement avec status=COMPLETED doit exister
     * 
     * @param orderId l'identifiant de la commande
     * @return OrderResponse la commande avec statut CONFIRMED
     * @throws OrderNotFoundException si la commande n'existe pas
     * @throws IllegalStateException si aucun paiement complété n'existe
     * 
     * Exigences: 4.3, 16.1
     */
    @Transactional
    public OrderResponse confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // Vérifier qu'un paiement complété existe (Exigence 4.3)
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        boolean hasCompletedPayment = payments.stream()
            .anyMatch(p -> "COMPLETED".equals(p.getStatus()));
        
        if (!hasCompletedPayment) {
            throw new IllegalStateException("Cannot confirm order without a completed payment");
        }
        
        // Mettre à jour le statut de la commande
        order.setStatus(OrderStatus.CONFIRMED);
        Order updatedOrder = orderRepository.save(order);
        
        // Publier l'événement order.confirmed
        Long paymentId = payments.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()))
            .findFirst()
            .map(Payment::getId)
            .orElse(null);
        orderEventProducer.publishOrderConfirmed(updatedOrder, paymentId);
        
        return mapToOrderResponse(updatedOrder);
    }
    
    /**
     * Annule une commande avec compensation (remboursement si paiement existe).
     * 
     * Processus Saga de compensation:
     * 1. Vérifie si un paiement complété existe
     * 2. Si oui, déclenche un remboursement via payment-service
     * 3. Met à jour le statut de la commande à CANCELLED
     * 4. Publie l'événement order.cancelled
     * 
     * @param orderId l'identifiant de la commande
     * @param reason la raison de l'annulation
     * @return OrderResponse la commande avec statut CANCELLED
     * @throws OrderNotFoundException si la commande n'existe pas
     * @throws RuntimeException si le remboursement échoue
     * 
     * Exigences: 4.6, 15.2
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // Étape 1 & 2: Vérifier et déclencher le remboursement si nécessaire (Exigence 4.6)
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        payments.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()))
            .findFirst()
            .ifPresent(payment -> {
                try {
                    // Appeler payment-service pour remboursement (Saga compensation)
                    paymentServiceClient.refundPayment(payment.getId());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to refund payment: " + e.getMessage(), e);
                }
            });
        
        // Étape 3: Mettre à jour le statut de la commande
        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        
        // Étape 4: Publier l'événement order.cancelled
        orderEventProducer.publishOrderCancelled(updatedOrder, reason);
        
        return mapToOrderResponse(updatedOrder);
    }
    
    /**
     * Récupère l'historique de suivi d'une commande.
     * Les événements sont triés par timestamp décroissant (plus récents en premier).
     * 
     * @param orderId l'identifiant de la commande
     * @return List<TrackingEventResponse> liste des événements de suivi
     * @throws OrderNotFoundException si la commande n'existe pas
     * 
     * Exigence: 4.8
     */
    public List<TrackingEventResponse> getTrackingHistory(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException(orderId);
        }
        
        List<TrackingEvent> events = trackingEventRepository.findByOrderIdOrderByTimestampDesc(orderId);
        return events.stream()
            .map(this::mapToTrackingEventResponse)
            .collect(Collectors.toList());
    }
    
    // ==================== Méthodes de Mapping ====================
    
    /**
     * Convertit une entité Order en OrderResponse DTO.
     */
    private OrderResponse mapToOrderResponse(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getClientId(),
            order.getParcelId(),
            order.getDriverId(),
            order.getStatus(),
            order.getTransportType(),
            order.getTotalPrice(),
            order.getScheduledAt(),
            order.getDeliveredAt()
        );
    }
    
    /**
     * Convertit une entité Payment en PaymentResponse DTO.
     */
    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getOrderId(),
            payment.getAmount(),
            payment.getMethod(),
            payment.getStatus(),
            payment.getPaidAt()
        );
    }
    
    /**
     * Convertit une entité TrackingEvent en TrackingEventResponse DTO.
     */
    private TrackingEventResponse mapToTrackingEventResponse(TrackingEvent event) {
        return new TrackingEventResponse(
            event.getId(),
            event.getOrderId(),
            event.getCity(),
            event.getLat(),
            event.getLng(),
            event.getStatus(),
            event.getTimestamp()
        );
    }
}
