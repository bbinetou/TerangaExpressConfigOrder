package sn.edu.ept.order_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignDriverRequest {
    @NotNull(message = "Driver ID is required")
    private Long driverId;
}
