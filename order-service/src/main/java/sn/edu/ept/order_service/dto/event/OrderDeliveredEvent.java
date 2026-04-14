package sn.edu.ept.order_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveredEvent {
    private Long orderId;
    private Long driverId;
    private LocalDateTime deliveredAt;
}
