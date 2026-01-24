package com.flightontime.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de predicción con features climáticas")
public record PredictionWithFeaturesDTO(
        @Schema(description = "Previsión del vuelo", example = "Puntual")
        String prevision,

        @Schema(description = "Probabilidad de la predicción", example = "0.85")
        double probabilidad,

        @Schema(description = "Datos climáticos utilizados en la predicción")
        WeatherFeaturesDTO clima
) {}
