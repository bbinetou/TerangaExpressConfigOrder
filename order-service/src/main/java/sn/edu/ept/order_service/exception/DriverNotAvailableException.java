package sn.edu.ept.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DriverNotAvailableException extends RuntimeException {
    public DriverNotAvailableException(Long driverId) {
        super(String.format("Driver with id %d is not available", driverId));
    }
    
    public DriverNotAvailableException(Long driverId, String reason) {
        super(String.format("Driver with id %d is not available: %s", driverId, reason));
    }
}
