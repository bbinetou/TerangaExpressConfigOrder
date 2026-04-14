package sn.edu.ept.order_service.controller;

import sn.edu.ept.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.order_service.dto.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des commandes de livraison.
 * 
 * Expose les endpoints suivants:
 * - POST /orders - Créer une commande
 * - GET /orders/{id} - Récupérer une commande
 * - GET /orders/client/{clientId} - Récupérer les commandes d'un client
 * - PUT /orders/{id}/status - Mettre à jour le statut
 * - POST /orders/{id}/confirm - Confirmer une commande
 * - POST /orders/{id}/cancel - Annuler une commande
 * - PUT /orders/{id}/assign-driver - Assigner un chauffeur
 * - POST /orders/{id}/tracking - Ajouter un événement de suivi
 * - GET /orders/{id}/tracking-history - Récupérer l'historique de suivi
 * - POST /orders/{id}/payment - Créer un paiement
 * 
 * Toutes les requêtes et réponses utilisent le format JSON.
 * Les validations sont effectuées via @Valid sur les DTOs.
 * 
 * @author Order Service Team
 * @version 1.0
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * Crée une nouvelle commande de livraison.
     * 
     * @param request les détails de la commande (clientId, parcelId, transportType, totalPrice, scheduledAt)
     * @return ResponseEntity<OrderResponse> 201 Created avec la commande créée
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Récupère une commande par son ID.
     * 
     * @param id l'identifiant de la commande
     * @return ResponseEntity<OrderResponse> 200 OK avec la commande
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Récupère toutes les commandes d'un client avec pagination.
     * 
     * @param clientId l'identifiant du client
     * @param pageable paramètres de pagination (page, size, sort)
     * @return ResponseEntity<Page<OrderResponse>> 200 OK avec la page de commandes
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByClient(
            @PathVariable Long clientId,
            Pageable pageable) {
        Page<OrderResponse> response = orderService.getOrdersByClient(clientId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Met à jour le statut d'une commande.
     * 
     * @param id l'identifiant de la commande
     * @param request le nouveau statut
     * @return ResponseEntity<OrderResponse> 200 OK avec la commande mise à jour
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        OrderResponse response = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Confirme une commande après paiement complété.
     * 
     * @param id l'identifiant de la commande
     * @return ResponseEntity<OrderResponse> 200 OK avec la commande confirmée
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long id) {
        OrderResponse response = orderService.confirmOrder(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Annule une commande avec compensation (remboursement si nécessaire).
     * 
     * @param id l'identifiant de la commande
     * @param request la raison de l'annulation
     * @return ResponseEntity<OrderResponse> 200 OK avec la commande annulée
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request) {
        OrderResponse response = orderService.cancelOrder(id, request.getReason());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Assigne un chauffeur à une commande.
     * 
     * @param id l'identifiant de la commande
     * @param request l'identifiant du chauffeur
     * @return ResponseEntity<OrderResponse> 200 OK avec la commande mise à jour
     */
    @PutMapping("/{id}/assign-driver")
    public ResponseEntity<OrderResponse> assignDriver(
            @PathVariable Long id,
            @Valid @RequestBody AssignDriverRequest request) {
        OrderResponse response = orderService.assignDriver(id, request.getDriverId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Ajoute un événement de suivi à une commande.
     * 
     * @param id l'identifiant de la commande
     * @param request les détails de l'événement (city, lat, lng, status, timestamp)
     * @return ResponseEntity<Void> 201 Created
     */
    @PostMapping("/{id}/tracking")
    public ResponseEntity<Void> addTrackingEvent(
            @PathVariable Long id,
            @Valid @RequestBody CreateTrackingEventRequest request) {
        orderService.addTrackingEvent(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    /**
     * Récupère l'historique de suivi d'une commande.
     * 
     * @param id l'identifiant de la commande
     * @return ResponseEntity<List<TrackingEventResponse>> 200 OK avec la liste des événements
     */
    @GetMapping("/{id}/tracking-history")
    public ResponseEntity<List<TrackingEventResponse>> getTrackingHistory(@PathVariable Long id) {
        List<TrackingEventResponse> response = orderService.getTrackingHistory(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Crée un paiement pour une commande.
     * 
     * @param id l'identifiant de la commande
     * @param request les détails du paiement (amount, method)
     * @return ResponseEntity<PaymentResponse> 201 Created avec le paiement créé
     */
    @PostMapping("/{id}/payment")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable Long id,
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = orderService.createPayment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
