package sn.edu.ept.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverAvailabilityResponse {
    private Long driverId;
    private Boolean available;
    private String currentCity;
}
