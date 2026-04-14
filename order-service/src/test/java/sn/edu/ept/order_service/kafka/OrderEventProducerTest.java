package sn.edu.ept.order_service.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import sn.edu.ept.order_service.dto.event.*;
import sn.edu.ept.order_service.entity.Order;
import sn.edu.ept.order_service.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderEventProducer orderEventProducer;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setClientId(100L);
        testOrder.setParcelId(200L);
        testOrder.setDriverId(300L);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTransportType("STANDARD");
        testOrder.setTotalPrice(50.0);
        testOrder.setScheduledAt(LocalDateTime.now());
    }

    @Test
    void testPublishOrderCreated() {
        // Arrange
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("order.created"), eq("1"), any(OrderCreatedEvent.class)))
                .thenReturn(future);

        // Act
        orderEventProducer.publishOrderCreated(testOrder);

        // Assert
        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(kafkaTemplate).send(eq("order.created"), eq("1"), eventCaptor.capture());

        OrderCreatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(1L, capturedEvent.getOrderId());
        assertEquals(100L, capturedEvent.getClientId());
        assertEquals(200L, capturedEvent.getParcelId());
        assertEquals(50.0, capturedEvent.getTotalPrice());
    }

    @Test
    void testPublishOrderConfirmed() {
        // Arrange
        Long paymentId = 500L;
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("order.confirmed"), eq("1"), any(OrderConfirmedEvent.class)))
                .thenReturn(future);

        // Act
        orderEventProducer.publishOrderConfirmed(testOrder, paymentId);

        // Assert
        ArgumentCaptor<OrderConfirmedEvent> eventCaptor = ArgumentCaptor.forClass(OrderConfirmedEvent.class);
        verify(kafkaTemplate).send(eq("order.confirmed"), eq("1"), eventCaptor.capture());

        OrderConfirmedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(1L, capturedEvent.getOrderId());
        assertEquals(500L, capturedEvent.getPaymentId());
    }

    @Test
    void testPublishOrderAssigned() {
        // Arrange
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("order.assigned"), eq("1"), any(OrderAssignedEvent.class)))
                .thenReturn(future);

        // Act
        orderEventProducer.publishOrderAssigned(testOrder);

        // Assert
        ArgumentCaptor<OrderAssignedEvent> eventCaptor = ArgumentCaptor.forClass(OrderAssignedEvent.class);
        verify(kafkaTemplate).send(eq("order.assigned"), eq("1"), eventCaptor.capture());

        OrderAssignedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(1L, capturedEvent.getOrderId());
        assertEquals(300L, capturedEvent.getDriverId());
    }

    @Test
    void testPublishOrderInTransit() {
        // Arrange
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("order.in-transit"), eq("1"), any(OrderInTransitEvent.class)))
                .thenReturn(future);

        // Act
        orderEventProducer.publishOrderInTransit(testOrder);

        // Assert
        ArgumentCaptor<OrderInTransitEvent> eventCaptor = ArgumentCaptor.forClass(OrderInTransitEvent.class);
        verify(kafkaTemplate).send(eq("order.in-transit"), eq("1"), eventCaptor.capture());

        OrderInTransitEvent capturedEvent = eventCaptor.getValue();
        assertEquals(1L, capturedEvent.getOrderId());
        assertEquals(300L, capturedEvent.getDriverId());
    }

    @Test
    void testPublishOrderDelivered() {
        // Arrange
        LocalDateTime deliveredAt = LocalDateTime.now();
        testOrder.setDeliveredAt(deliveredAt);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("order.delivered"), eq("1"), any(OrderDeliveredEvent.class)))
                .thenReturn(future);

        // Act
        orderEventProducer.publishOrderDelivered(testOrder);

        // Assert
        ArgumentCaptor<OrderDeliveredEvent> eventCaptor = ArgumentCaptor.forClass(OrderDeliveredEvent.class);
        verify(kafkaTemplate).send(eq("order.delivered"), eq("1"), eventCaptor.capture());

        OrderDeliveredEvent capturedEvent = eventCaptor.getValue();
        assertEquals(1L, capturedEvent.getOrderId());
        assertEquals(300L, capturedEvent.getDriverId());
        assertNotNull(capturedEvent.getDeliveredAt());
    }

    @Test
    void testPublishOrderCancelled() {
        // Arrange
        String reason = "Client request";
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("order.cancelled"), eq("1"), any(OrderCancelledEvent.class)))
                .thenReturn(future);

        // Act
        orderEventProducer.publishOrderCancelled(testOrder, reason);

        // Assert
        ArgumentCaptor<OrderCancelledEvent> eventCaptor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(kafkaTemplate).send(eq("order.cancelled"), eq("1"), eventCaptor.capture());

        OrderCancelledEvent capturedEvent = eventCaptor.getValue();
        assertEquals(1L, capturedEvent.getOrderId());
        assertEquals("Client request", capturedEvent.getReason());
    }

    @Test
    void testPublishOrderCreatedHandlesException() {
        // Arrange
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Kafka error"));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> orderEventProducer.publishOrderCreated(testOrder));
    }
}
