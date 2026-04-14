package sn.edu.ept.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrackingEventRequest {
    @NotBlank(message = "City is required")
    private String city;
    
    @NotNull(message = "Latitude is required")
    private Double lat;
    
    @NotNull(message = "Longitude is required")
    private Double lng;
    
    @NotBlank(message = "Status is required")
    private String status;
    
    private LocalDateTime timestamp;
}
