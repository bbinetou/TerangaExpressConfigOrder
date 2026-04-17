package sn.edu.ept.order_service.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    
    @Bean
    public CustomFeignErrorDecoder customFeignErrorDecoder() {
        return new CustomFeignErrorDecoder();
    }
}
