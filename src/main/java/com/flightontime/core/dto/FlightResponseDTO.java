package com.flightontime.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flightontime.core.model.EstadoVuelo;
import com.flightontime.core.model.PredictionResult;

import java.time.LocalDateTime;

public record FlightResponseDTO(
        @JsonProperty("nombre_aerolinea")
        String nombreAerolinea,
        @JsonProperty("nombre_aeropuerto_origen")
        String nombreAeropuertoOrigen,
        @JsonProperty("nombre_aeropuerto_destino")
        String nombreAeropuertoDestino,
        @JsonProperty("fecha_partida")
        LocalDateTime fechaYHoraPartida,
        EstadoVuelo prevision
) {
    public static FlightResponseDTO fromEntity(PredictionResult prediccion){
        return new FlightResponseDTO(
                prediccion.getFlight().getAerolinea().getNombre(),
                prediccion.getFlight().getOrigen().getNombre(),
                prediccion.getFlight().getDestino().getNombre(),
                prediccion.getFlight().getFechaPartida(),
                prediccion.getPrevision()
        );
    }
}
