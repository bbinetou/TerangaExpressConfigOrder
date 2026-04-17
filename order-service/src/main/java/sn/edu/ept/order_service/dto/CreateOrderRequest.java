package sn.edu.ept.order_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    // Client ID sera automatiquement rempli depuis @CurrentUser dans le controller
    private Long clientId;
    
    @NotNull(message = "Parcel ID is required")
    private Long parcelId;
    
    @NotBlank(message = "Transport type is required")
    private String transportType;
    
    @NotNull(message = "Total price is required")
    @Positive(message = "Total price must be positive")
    private Double totalPrice;
    
    @NotNull(message = "Scheduled date is required")
    private LocalDateTime scheduledAt;
}
