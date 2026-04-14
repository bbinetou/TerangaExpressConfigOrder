package sn.edu.ept.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEventResponse {
    private Long id;
    private Long orderId;
    private String city;
    private Double lat;
    private Double lng;
    private String status;
    private LocalDateTime timestamp;
}
