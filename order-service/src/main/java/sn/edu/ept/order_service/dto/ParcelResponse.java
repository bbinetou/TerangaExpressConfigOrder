package sn.edu.ept.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelResponse {
    private UUID id;
    private UUID senderId;
    private String description;
    private String type;
    private Double weight;
    private Double volumeM3;
    private String status;
    private String originCity;
    private String destinationCity;
    private LocalDateTime createdAt;
}
