package sn.edu.ept.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import sn.edu.ept.order_service.dto.UserExistsResponse;
import sn.edu.ept.order_service.dto.UserResponse;

@FeignClient(name = "user-service", url = "${user-service.url:}")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    UserResponse getUser(@PathVariable("id") Long id);
    
    @GetMapping("/api/users/{id}/exists")
    UserExistsResponse validateUserExists(@PathVariable("id") Long id);
}
