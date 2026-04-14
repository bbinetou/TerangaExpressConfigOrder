package sn.edu.ept.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelResponse {
    private Long id;
    private Long senderId;
    private String description;
    private String type;
    private Double weight;
    private Double volumeM3;
    private String status;
    private String originCity;
    private String destinationCity;
    private LocalDateTime createdAt;
}
