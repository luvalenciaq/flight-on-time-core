package com.flightontime.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flightontime.core.model.Flight;

import java.time.LocalDateTime;

public record FlightRequestDTO(
        String aerolinea,
        String origen,
        String destino,
        LocalDateTime fechaPartida,
        double distanciaKm
) {
    public FlightRequestDTO(Flight flight){
        this(flight.getAerolinea(), flight.getOrigen(), flight.getDestino(), flight.getFechaPartida(), flight.getDistanciaKm());
    }
}
