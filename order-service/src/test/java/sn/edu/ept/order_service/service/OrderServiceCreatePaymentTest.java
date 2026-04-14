package sn.edu.ept.order_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.edu.ept.order_service.dto.CreatePaymentRequest;
import sn.edu.ept.order_service.dto.PaymentResponse;
import sn.edu.ept.order_service.entity.Order;
import sn.edu.ept.order_service.entity.OrderStatus;
import sn.edu.ept.order_service.entity.Payment;
import sn.edu.ept.order_service.exception.OrderNotFoundException;
import sn.edu.ept.order_service.repository.OrderRepository;
import sn.edu.ept.order_service.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService.createPayment method
 * Validates Requirements 6.1, 6.2, and 17.1
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceCreatePaymentTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private CreatePaymentRequest validPaymentRequest;

    @BeforeEach
    void setUp() {
        // Create test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setClientId(100L);
        testOrder.setParcelId(200L);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalPrice(150.0);
        testOrder.setTransportType("STANDARD");
        testOrder.setScheduledAt(LocalDateTime.now().plusDays(1));

        // Create valid payment request
        validPaymentRequest = new CreatePaymentRequest();
        validPaymentRequest.setAmount(150.0);
        validPaymentRequest.setMethod("CARD");
    }

    @Test
    void createPayment_WithValidAmountAndNoCompletedPayment_ShouldCreatePaymentWithPendingStatus() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Collections.emptyList());
        
        Payment savedPayment = new Payment();
        savedPayment.setId(1L);
        savedPayment.setOrderId(1L);
        savedPayment.setAmount(150.0);
        savedPayment.setMethod("CARD");
        savedPayment.setStatus("PENDING");
        savedPayment.setPaidAt(LocalDateTime.now());
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // When
        PaymentResponse response = orderService.createPayment(1L, validPaymentRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getOrderId());
        assertEquals(150.0, response.getAmount());
        assertEquals("CARD", response.getMethod());
        assertEquals("PENDING", response.getStatus());
        assertNotNull(response.getPaidAt());
        
        // Verify payment was saved with PENDING status
        verify(paymentRepository).save(argThat(payment -> 
            payment.getOrderId().equals(1L) &&
            payment.getAmount().equals(150.0) &&
            payment.getMethod().equals("CARD") &&
            payment.getStatus().equals("PENDING")
        ));
    }

    @Test
    void createPayment_WithAmountNotMatchingTotalPrice_ShouldThrowException() {
        // Given
        CreatePaymentRequest invalidRequest = new CreatePaymentRequest();
        invalidRequest.setAmount(100.0); // Different from order totalPrice (150.0)
        invalidRequest.setMethod("CARD");
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.createPayment(1L, invalidRequest)
        );
        
        assertTrue(exception.getMessage().contains("Payment amount"));
        assertTrue(exception.getMessage().contains("does not match order totalPrice"));
        
        // Verify payment was NOT saved
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_WithExistingCompletedPayment_ShouldThrowException() {
        // Given
        Payment completedPayment = new Payment();
        completedPayment.setId(1L);
        completedPayment.setOrderId(1L);
        completedPayment.setAmount(150.0);
        completedPayment.setMethod("CARD");
        completedPayment.setStatus("COMPLETED");
        completedPayment.setPaidAt(LocalDateTime.now());
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Collections.singletonList(completedPayment));

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> orderService.createPayment(1L, validPaymentRequest)
        );
        
        assertEquals("Order 1 already has a completed payment", exception.getMessage());
        
        // Verify payment was NOT saved
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_WithMultiplePaymentsIncludingCompleted_ShouldThrowException() {
        // Given
        Payment pendingPayment = new Payment();
        pendingPayment.setId(1L);
        pendingPayment.setOrderId(1L);
        pendingPayment.setAmount(150.0);
        pendingPayment.setMethod("CARD");
        pendingPayment.setStatus("PENDING");
        pendingPayment.setPaidAt(LocalDateTime.now());
        
        Payment completedPayment = new Payment();
        completedPayment.setId(2L);
        completedPayment.setOrderId(1L);
        completedPayment.setAmount(150.0);
        completedPayment.setMethod("MOBILE_MONEY");
        completedPayment.setStatus("COMPLETED");
        completedPayment.setPaidAt(LocalDateTime.now());
        
        List<Payment> payments = Arrays.asList(pendingPayment, completedPayment);
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(payments);

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> orderService.createPayment(1L, validPaymentRequest)
        );
        
        assertEquals("Order 1 already has a completed payment", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_WithExistingPendingPayment_ShouldAllowCreation() {
        // Given
        Payment pendingPayment = new Payment();
        pendingPayment.setId(1L);
        pendingPayment.setOrderId(1L);
        pendingPayment.setAmount(150.0);
        pendingPayment.setMethod("CARD");
        pendingPayment.setStatus("PENDING");
        pendingPayment.setPaidAt(LocalDateTime.now());
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Collections.singletonList(pendingPayment));
        
        Payment savedPayment = new Payment();
        savedPayment.setId(2L);
        savedPayment.setOrderId(1L);
        savedPayment.setAmount(150.0);
        savedPayment.setMethod("MOBILE_MONEY");
        savedPayment.setStatus("PENDING");
        savedPayment.setPaidAt(LocalDateTime.now());
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // When
        PaymentResponse response = orderService.createPayment(1L, validPaymentRequest);

        // Then
        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPayment_WithExistingFailedPayment_ShouldAllowCreation() {
        // Given
        Payment failedPayment = new Payment();
        failedPayment.setId(1L);
        failedPayment.setOrderId(1L);
        failedPayment.setAmount(150.0);
        failedPayment.setMethod("CARD");
        failedPayment.setStatus("FAILED");
        failedPayment.setPaidAt(LocalDateTime.now());
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Collections.singletonList(failedPayment));
        
        Payment savedPayment = new Payment();
        savedPayment.setId(2L);
        savedPayment.setOrderId(1L);
        savedPayment.setAmount(150.0);
        savedPayment.setMethod("MOBILE_MONEY");
        savedPayment.setStatus("PENDING");
        savedPayment.setPaidAt(LocalDateTime.now());
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // When
        PaymentResponse response = orderService.createPayment(1L, validPaymentRequest);

        // Then
        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPayment_WithNonExistentOrder_ShouldThrowOrderNotFoundException() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
            OrderNotFoundException.class,
            () -> orderService.createPayment(999L, validPaymentRequest)
        );
        
        // Verify payment repository was never called
        verify(paymentRepository, never()).findByOrderId(any());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_WithDifferentPaymentMethods_ShouldCreatePaymentWithPendingStatus() {
        // Given
        String[] methods = {"CARD", "MOBILE_MONEY", "CASH"};
        
        for (String method : methods) {
            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setAmount(150.0);
            request.setMethod(method);
            
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(paymentRepository.findByOrderId(1L)).thenReturn(Collections.emptyList());
            
            Payment savedPayment = new Payment();
            savedPayment.setId(1L);
            savedPayment.setOrderId(1L);
            savedPayment.setAmount(150.0);
            savedPayment.setMethod(method);
            savedPayment.setStatus("PENDING");
            savedPayment.setPaidAt(LocalDateTime.now());
            
            when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

            // When
            PaymentResponse response = orderService.createPayment(1L, request);

            // Then
            assertNotNull(response);
            assertEquals(method, response.getMethod());
            assertEquals("PENDING", response.getStatus());
        }
    }

    @Test
    void createPayment_WithExactAmountMatch_ShouldSucceed() {
        // Given
        testOrder.setTotalPrice(99.99);
        validPaymentRequest.setAmount(99.99);
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Collections.emptyList());
        
        Payment savedPayment = new Payment();
        savedPayment.setId(1L);
        savedPayment.setOrderId(1L);
        savedPayment.setAmount(99.99);
        savedPayment.setMethod("CARD");
        savedPayment.setStatus("PENDING");
        savedPayment.setPaidAt(LocalDateTime.now());
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // When
        PaymentResponse response = orderService.createPayment(1L, validPaymentRequest);

        // Then
        assertNotNull(response);
        assertEquals(99.99, response.getAmount());
        verify(paymentRepository).save(any(Payment.class));
    }
}
