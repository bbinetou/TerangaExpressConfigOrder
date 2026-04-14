package sn.edu.ept.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {
    private Long id;
    private Long userId;
    private String vehicleType;
    private String licensePlate;
    private Boolean available;
    private Double rating;
    private String currentCity;
    private LocalDateTime createdAt;
}
