package sn.edu.ept.order_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.edu.ept.order_service.dto.OrderResponse;
import sn.edu.ept.order_service.entity.Order;
import sn.edu.ept.order_service.entity.OrderStatus;
import sn.edu.ept.order_service.entity.Payment;
import sn.edu.ept.order_service.exception.OrderNotFoundException;
import sn.edu.ept.order_service.kafka.OrderEventProducer;
import sn.edu.ept.order_service.repository.OrderRepository;
import sn.edu.ept.order_service.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService.confirmOrder method
 * Validates Requirements 4.3 and 17.1
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceConfirmOrderTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private Payment completedPayment;
    private Payment pendingPayment;

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

        // Create completed payment
        completedPayment = new Payment();
        completedPayment.setId(1L);
        completedPayment.setOrderId(1L);
        completedPayment.setAmount(150.0);
        completedPayment.setMethod("CARD");
        completedPayment.setStatus("COMPLETED");
        completedPayment.setPaidAt(LocalDateTime.now());

        // Create pending payment
        pendingPayment = new Payment();
        pendingPayment.setId(2L);
        pendingPayment.setOrderId(1L);
        pendingPayment.setAmount(150.0);
        pendingPayment.setMethod("CARD");
        pendingPayment.setStatus("PENDING");
        pendingPayment.setPaidAt(LocalDateTime.now());
    }

    @Test
    void confirmOrder_WithCompletedPayment_ShouldConfirmOrder() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Collections.singletonList(completedPayment));
        
        Order confirmedOrder = new Order();
        confirmedOrder.setId(testOrder.getId());
        confirmedOrder.setClientId(testOrder.getClientId());
        confirmedOrder.setParcelId(testOrder.getParcelId());
        confirmedOrder.setStatus(OrderStatus.CONFIRMED);
        confirmedOrder.setTotalPrice(testOrder.getTotalPrice());
        confirmedOrder.setTransportType(testOrder.getTransportType());
        confirmedOrder.setScheduledAt(testOrder.getScheduledAt());
        
        when(orderRepository.save(any(Order.class))).thenReturn(confirmedOrder);

        // When
        OrderResponse response = orderService.confirmOrder(1L);

        // Then
        assertNotNull(response);
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        assertEquals(1L, response.getId());
        
        // Verify order was saved with CONFIRMED status
        verify(orderRepository).save(argThat(order -> 
            order.getStatus() == OrderStatus.CONFIRMED && order.getId().equals(1L)
        ));
        
        // Verify event was published
        verify(orderEventProducer).publishOrderConfirmed(any(Order.class), eq(1L));
    }

    @Test
    void confirmOrder_WithMultiplePaymentsIncludingCompleted_ShouldConfirmOrder() {
        // Given
        List<Payment> payments = Arrays.asList(pendingPayment, completedPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(payments);
        
        Order confirmedOrder = new Order();
        confirmedOrder.setId(testOrder.getId());
        confirmedOrder.setClientId(testOrder.getClientId());
        confirmedOrder.setParcelId(testOrder.getParcelId());
        confirmedOrder.setStatus(OrderStatus.CONFIRMED);
        confirmedOrder.setTotalPrice(testOrder.getTotalPrice());
        confirmedOrder.setTransportType(testOrder.getTransportType());
        confirmedOrder.setScheduledAt(testOrder.getScheduledAt());
        
        when(orderRepository.save(any(Order.class))).thenReturn(confirmedOrder);

        // When
        OrderResponse response = orderService.confirmOrder(1L);

        // Then
        assertNotNull(response);
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        verify(orderRepository).save(any(Order.class));
        verify(orderEventProducer).publishOrderConfirmed(any(Order.class), eq(1L));
    }

    @Test
    void confirmOrder_WithoutCompletedPayment_ShouldThrowException() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Collections.singletonList(pendingPayment));

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> orderService.confirmOrder(1L)
        );
        
        assertEquals("Cannot confirm order without a completed payment", exception.getMessage());
        
        // Verify order was NOT saved
        verify(orderRepository, never()).save(any(Order.class));
        
        // Verify event was NOT published
        verify(orderEventProducer, never()).publishOrderConfirmed(any(Order.class), any());
    }

    @Test
    void confirmOrder_WithNoPayments_ShouldThrowException() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Collections.emptyList());

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> orderService.confirmOrder(1L)
        );
        
        assertEquals("Cannot confirm order without a completed payment", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderEventProducer, never()).publishOrderConfirmed(any(Order.class), any());
    }

    @Test
    void confirmOrder_WithNonExistentOrder_ShouldThrowOrderNotFoundException() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
            OrderNotFoundException.class,
            () -> orderService.confirmOrder(999L)
        );
        
        // Verify payment repository was never called
        verify(paymentRepository, never()).findByOrderId(any());
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderEventProducer, never()).publishOrderConfirmed(any(Order.class), any());
    }

    @Test
    void confirmOrder_WithFailedPayment_ShouldThrowException() {
        // Given
        Payment failedPayment = new Payment();
        failedPayment.setId(3L);
        failedPayment.setOrderId(1L);
        failedPayment.setAmount(150.0);
        failedPayment.setMethod("CARD");
        failedPayment.setStatus("FAILED");
        failedPayment.setPaidAt(LocalDateTime.now());
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Collections.singletonList(failedPayment));

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> orderService.confirmOrder(1L)
        );
        
        assertEquals("Cannot confirm order without a completed payment", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }
}
