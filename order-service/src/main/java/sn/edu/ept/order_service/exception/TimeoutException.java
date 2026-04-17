package sn.edu.ept.order_service.exception;

public class TimeoutException extends RuntimeException {
    private final long timeoutDuration;
    
    public TimeoutException(long timeoutDuration, String message) {
        super(message);
        this.timeoutDuration = timeoutDuration;
    }
    
    public long getTimeoutDuration() {
        return timeoutDuration;
    }
}
