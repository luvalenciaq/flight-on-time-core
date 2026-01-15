package com.flightontime.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flightontime.core.model.Flight;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Datos de entrada para la predicción de puntualidad")
public record FlightRequestDTO(
        @Schema(description = "Código de la aerolínea", example = "AA") String aerolinea,
        @Schema(description = "Código del aeropuerto de origen", example = "JFK") String origen,
        @Schema(description = "Código del aeropuerto de destino", example = "LAX") String destino,
        @JsonProperty("fecha_partida")
        @Schema(description = "Fecha y hora de partida programada", example = "2026-10-27T10:00:00") LocalDateTime fechaPartida
) { //se borra distancia aqui tambien porque es el contrato con bff
    public FlightRequestDTO(Flight flight){
        this(flight.getAerolinea().getCodigo(), flight.getOrigen().getCodigo(), flight.getDestino().getCodigo(), flight.getFechaPartida());
    }
}
