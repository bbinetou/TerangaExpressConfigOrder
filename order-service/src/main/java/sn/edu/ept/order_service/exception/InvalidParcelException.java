package sn.edu.ept.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidParcelException extends RuntimeException {
    public InvalidParcelException(String message) {
        super(message);
    }
    
    public InvalidParcelException(Long parcelId, String reason) {
        super(String.format("Invalid parcel with id %d: %s", parcelId, reason));
    }
}
