package sn.edu.ept.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TariffResponse {
    private Long parcelId;
    private Double tariff;
    private Double basePrice;
    private Double weightCost;
    private Double volumeCost;
    private Double distanceCost;
}
