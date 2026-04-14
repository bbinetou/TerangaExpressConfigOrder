package sn.edu.ept.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
    
    public PaymentException(Long orderId, String reason) {
        super(String.format("Payment error for order %d: %s", orderId, reason));
    }
}
