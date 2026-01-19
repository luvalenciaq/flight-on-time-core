package com.flightontime.core.dto;

public record WeatherDataDTO(//DTO con datos meteorológicos procesados para el modelo
                             float tempRangeF,
                             float dewpointRangeF,
                             float hasPrecip,
                             float hasSnow,
                             float highWind) {
}
