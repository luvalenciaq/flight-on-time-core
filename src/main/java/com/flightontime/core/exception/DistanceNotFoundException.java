package com.flightontime.core.exception;

public class DistanceNotFoundException extends RuntimeException {

    public DistanceNotFoundException(String origen, String destino) {
        super(String.format("No existe ruta directa de %s a %s", origen, destino));
    }
}
