package sn.edu.ept.order_service.dto;

import sn.edu.ept.order_service.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long clientId;
    private UUID parcelId;
    private Long driverId;
    private OrderStatus status;
    private String transportType;
    private Double totalPrice;
    private LocalDateTime scheduledAt;
    private LocalDateTime deliveredAt;
}
