package sn.edu.ept.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import sn.edu.ept.order_service.dto.ParcelResponse;
import sn.edu.ept.order_service.dto.TariffResponse;

/**
 * Feign client for communicating with parcel-service
 * Uses Eureka service discovery to locate parcel-service instances
 */
@FeignClient(name = "parcel-service")
public interface ParcelServiceClient {
    
    /**
     * Get parcel details by ID
     * @param id the parcel ID
     * @return ParcelResponse containing parcel details
     */
    @GetMapping("/api/parcels/{id}")
    ParcelResponse getParcel(@PathVariable("id") Long id);
    
    /**
     * Calculate tariff for a parcel
     * @param id the parcel ID
     * @return TariffResponse containing calculated tariff
     */
    @PostMapping("/api/parcels/{id}/calculate-tariff")
    TariffResponse calculateTariff(@PathVariable("id") Long id);
}
