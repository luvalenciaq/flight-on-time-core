package com.flightontime.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de la predicción de puntualidad")
public record PredictionResponseDTO(
        @Schema(description = "Previsión de puntualidad (ej. 'Puntual', 'Retrasado')", example = "Puntual") String prevision,
        @Schema(description = "Probabilidad calculada de puntualidad (0.0 a 1.0)", example = "0.85") double probabilidad) {
}
