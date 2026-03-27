package edu.sjsu.cmpe172.salon.exception;

public class SlotReservationConflictException extends IllegalArgumentException {
    public SlotReservationConflictException(String message) {
        super(message);
    }

    public SlotReservationConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
