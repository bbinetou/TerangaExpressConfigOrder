package sn.edu.ept.order_service.exception;

import sn.edu.ept.order_service.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestionnaire global des exceptions pour order-service.
 * 
 * Intercepte toutes les exceptions levées par les contrôleurs et les convertit
 * en réponses HTTP standardisées avec format JSON uniforme.
 * 
 * Format de réponse d'erreur:
 * {
 *   "timestamp": "2026-04-13T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Order with id 10 not found",
 *   "path": "/orders/10"
 * }
 * 
 * Codes HTTP utilisés:
 * - 400 Bad Request: Validation échouée, données invalides
 * - 404 Not Found: Ressource introuvable
 * - 409 Conflict: Conflit métier (paiement, chauffeur indisponible)
 * - 500 Internal Server Error: Erreur interne inattendue
 * 
 * Exigences: 13.1-13.7
 * 
 * @author Order Service Team
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Gère les exceptions OrderNotFoundException.
     * Retourne 404 Not Found.
     * 
     * @param ex l'exception levée
     * @param request la requête HTTP
     * @return ResponseEntity avec ErrorResponse et code 404
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex, WebRequest request) {
        logger.error("Order not found: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            getPath(request)
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Gère les exceptions ValidationException.
     * Retourne 400 Bad Request.
     * 
     * @param ex l'exception levée
     * @param request la requête HTTP
     * @return ResponseEntity avec ErrorResponse et code 400
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, WebRequest request) {
        logger.error("Validation error: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            getPath(request)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Gère les exceptions InvalidParcelException.
     * Retourne 400 Bad Request.
     * 
     * @param ex l'exception levée
     * @param request la requête HTTP
     * @return ResponseEntity avec ErrorResponse et code 400
     */
    @ExceptionHandler(InvalidParcelException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParcel(InvalidParcelException ex, WebRequest request) {
        logger.error("Invalid parcel: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            getPath(request)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Gère les exceptions PaymentException.
     * Retourne 409 Conflict (conflit métier lié au paiement).
     * 
     * @param ex l'exception levée
     * @param request la requête HTTP
     * @return ResponseEntity avec ErrorResponse et code 409
     */
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePayment(PaymentException ex, WebRequest request) {
        logger.error("Payment error: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.getReasonPhrase(),
            ex.getMessage(),
            getPath(request)
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Gère les exceptions DriverNotAvailableException.
     * Retourne 409 Conflict (chauffeur indisponible).
     * 
     * @param ex l'exception levée
     * @param request la requête HTTP
     * @return ResponseEntity avec ErrorResponse et code 409
     */
    @ExceptionHandler(DriverNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleDriverNotAvailable(DriverNotAvailableException ex, WebRequest request) {
        logger.error("Driver not available: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.getReasonPhrase(),
            ex.getMessage(),
            getPath(request)
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Gère les exceptions IllegalStateException.
     * Retourne 409 Conflict (état invalide pour l'opération).
     * 
     * @param ex l'exception levée
     * @param request la requête HTTP
     * @return ResponseEntity avec ErrorResponse et code 409
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, WebRequest request) {
        logger.error("Illegal state: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.getReasonPhrase(),
            ex.getMessage(),
            getPath(request)
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Gère les erreurs de validation Bean Validation (@Valid).
     * Retourne 400 Bad Request avec la liste des erreurs de validation.
     * 
     * @param ex l'exception levée
     * @param request la requête HTTP
     * @return ResponseEntity avec ErrorResponse et code 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        logger.error("Validation error: {}", ex.getMessage(), ex);
        
        // Extraire tous les messages d'erreur de validation
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Validation failed: " + String.join(", ", errors),
            getPath(request)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Gère toutes les exceptions non gérées spécifiquement.
     * Retourne 500 Internal Server Error.
     * 
     * @param ex l'exception levée
     * @param request la requête HTTP
     * @return ResponseEntity avec ErrorResponse et code 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Internal error: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "An internal error occurred",
            getPath(request)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Extrait le chemin de la requête HTTP.
     * 
     * @param request la requête HTTP
     * @return le chemin de la requête (ex: /orders/10)
     */
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
