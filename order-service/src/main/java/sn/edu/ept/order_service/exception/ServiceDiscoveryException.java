package sn.edu.ept.order_service.exception;

public class ServiceDiscoveryException extends RuntimeException {
    private final String serviceName;
    
    public ServiceDiscoveryException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
}
