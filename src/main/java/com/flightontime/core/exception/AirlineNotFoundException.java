package com.flightontime.core.exception;

public class AirlineNotFoundException extends RuntimeException {

    public AirlineNotFoundException(String codigo) {
        super("Aerolínea no encontrada: " + codigo);
    }
}