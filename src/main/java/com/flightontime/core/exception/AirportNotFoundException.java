package com.flightontime.core.exception;

public class AirportNotFoundException extends RuntimeException {

    public AirportNotFoundException(String codigo) {
        super("Aeropuerto no encontrado: " + codigo);
    }
}
