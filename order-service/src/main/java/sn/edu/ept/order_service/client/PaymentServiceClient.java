package sn.edu.ept.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import sn.edu.ept.order_service.dto.CreatePaymentRequest;
import sn.edu.ept.order_service.dto.PaymentResponse;

/**
 * Feign client for communicating with payment-service
 * Uses Eureka service discovery to locate payment-service instances
 */
@FeignClient(name = "payment-service")
public interface PaymentServiceClient {
    
    /**
     * Create a new payment
     * @param request the payment creation request
     * @return PaymentResponse containing created payment details
     */
    @PostMapping("/api/payments")
    PaymentResponse createPayment(@RequestBody CreatePaymentRequest request);
    
    /**
     * Confirm a payment
     * @param id the payment ID
     * @return PaymentResponse with updated payment status
     */
    @PostMapping("/api/payments/{id}/confirm")
    PaymentResponse confirmPayment(@PathVariable("id") Long id);
    
    /**
     * Refund a payment
     * @param id the payment ID
     * @return PaymentResponse with refunded payment status
     */
    @PostMapping("/api/payments/{id}/refund")
    PaymentResponse refundPayment(@PathVariable("id") Long id);
}
