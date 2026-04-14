package sn.edu.ept.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import sn.edu.ept.order_service.dto.GroupingResponse;

/**
 * Feign client for communicating with grouping-service
 * Uses Eureka service discovery to locate grouping-service instances
 */
@FeignClient(name = "grouping-service")
public interface GroupingServiceClient {
    
    /**
     * Add an order to a delivery group
     * @param orderId the order ID to add to a group
     * @return GroupingResponse containing group details
     */
    @PostMapping("/api/grouping/orders/{orderId}")
    GroupingResponse addToGroup(@PathVariable("orderId") Long orderId);
}
