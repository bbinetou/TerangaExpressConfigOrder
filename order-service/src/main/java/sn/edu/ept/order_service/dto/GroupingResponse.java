package sn.edu.ept.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupingResponse {
    private Long id;
    private List<Long> orderIds;
    private String route;
    private String departureCity;
    private String arrivalCity;
    private Double optimizedDistanceKm;
    private String groupStatus;
    private LocalDateTime createdAt;
}
