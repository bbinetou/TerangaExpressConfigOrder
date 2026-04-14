package sn.edu.ept.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import sn.edu.ept.order_service.dto.AssignDriverRequest;
import sn.edu.ept.order_service.dto.DriverAvailabilityResponse;
import sn.edu.ept.order_service.dto.DriverResponse;

/**
 * Feign client for communicating with driver-service
 * Uses Eureka service discovery to locate driver-service instances
 */
@FeignClient(name = "driver-service")
public interface DriverServiceClient {
    
    /**
     * Get driver details by ID
     * @param id the driver ID
     * @return DriverResponse containing driver details
     */
    @GetMapping("/api/drivers/{id}")
    DriverResponse getDriver(@PathVariable("id") Long id);
    
    /**
     * Check driver availability
     * @param id the driver ID
     * @return DriverAvailabilityResponse containing availability status
     */
    @GetMapping("/api/drivers/{id}/availability")
    DriverAvailabilityResponse checkAvailability(@PathVariable("id") Long id);
    
    /**
     * Assign driver to an order
     * @param request the assignment request containing driver details
     * @return DriverResponse with updated driver information
     */
    @PostMapping("/api/drivers/assign")
    DriverResponse assignDriver(@RequestBody AssignDriverRequest request);
}
