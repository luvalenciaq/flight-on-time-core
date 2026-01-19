package com.flightontime.core.exception;

public class FlightValidationException extends RuntimeException{
    public FlightValidationException(String message) {
        super(message);
    }
}
