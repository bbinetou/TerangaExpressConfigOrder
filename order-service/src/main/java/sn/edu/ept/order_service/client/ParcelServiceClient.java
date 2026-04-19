package sn.edu.ept.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import sn.edu.ept.order_service.dto.ParcelResponse;
import sn.edu.ept.order_service.dto.TariffResponse;

import java.util.UUID;

@FeignClient(name = "parcel-service", url = "${parcel-service.url:}")
public interface ParcelServiceClient {

    @GetMapping("/api/parcels/{id}")
    ParcelResponse getParcel(@PathVariable("id") UUID id);

    @PostMapping("/api/parcels/{id}/calculate-tariff")
    TariffResponse calculateTariff(@PathVariable("id") UUID id);
}
