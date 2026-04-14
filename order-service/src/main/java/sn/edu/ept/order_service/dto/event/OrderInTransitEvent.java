package sn.edu.ept.order_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderInTransitEvent {
    private Long orderId;
    private Long driverId;
}
