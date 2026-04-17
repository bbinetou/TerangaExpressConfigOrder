package sn.edu.ept.order_service.exception;

public class ServiceUnavailableException extends RuntimeException {
    private final String serviceName;
    
    public ServiceUnavailableException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
}
