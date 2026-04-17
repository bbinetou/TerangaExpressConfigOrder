package sn.edu.ept.order_service.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sn.edu.ept.order_service.exception.ResourceNotFoundException;
import sn.edu.ept.order_service.exception.ServiceUnavailableException;

@Component
public class CustomFeignErrorDecoder implements ErrorDecoder {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomFeignErrorDecoder.class);
    private final ErrorDecoder defaultDecoder = new Default();
    
    @Override
    public Exception decode(String methodKey, Response response) {
        String serviceName = extractServiceName(methodKey);
        
        logger.error("Feign client error - Method: {}, Status: {}, Service: {}", 
                     methodKey, response.status(), serviceName);
        
        switch (response.status()) {
            case 404:
                return new ResourceNotFoundException(
                    String.format("Resource not found in %s", serviceName));
            case 500:
                return new ServiceUnavailableException(serviceName,
                    String.format("Internal server error in %s", serviceName));
            case 503:
                return new ServiceUnavailableException(serviceName,
                    String.format("Service %s is unavailable", serviceName));
            default:
                return defaultDecoder.decode(methodKey, response);
        }
    }
    
    private String extractServiceName(String methodKey) {
        // Extract service name from methodKey (e.g., "ParcelServiceClient#getParcel(Long)")
        if (methodKey != null && methodKey.contains("#")) {
            return methodKey.substring(0, methodKey.indexOf('#'));
        }
        return "Unknown Service";
    }
}
