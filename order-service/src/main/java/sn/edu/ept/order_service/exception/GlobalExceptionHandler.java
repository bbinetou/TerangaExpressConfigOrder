package sn.edu.ept.order_service.exception;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sn.edu.ept.order_service.dto.ErrorResponse;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        logger.error("Resource not found: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            ""
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(ServiceUnavailableException ex) {
        logger.error("Service unavailable: {} - {}", ex.getServiceName(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "Service Unavailable",
            ex.getMessage(),
            ""
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
    
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeout(TimeoutException ex) {
        logger.error("Timeout after {}ms: {}", ex.getTimeoutDuration(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.REQUEST_TIMEOUT.value(),
            "Request Timeout",
            ex.getMessage(),
            ""
        );
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(error);
    }
    
    @ExceptionHandler(ServiceDiscoveryException.class)
    public ResponseEntity<ErrorResponse> handleServiceDiscovery(ServiceDiscoveryException ex) {
        logger.error("Service discovery failed for {}: {}", ex.getServiceName(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "Service Unavailable",
            ex.getMessage(),
            ""
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
    
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex) {
        logger.error("Feign exception: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            ex.status(),
            "Feign Client Error",
            "Error communicating with external service: " + ex.getMessage(),
            ""
        );
        return ResponseEntity.status(ex.status()).body(error);
    }
}
