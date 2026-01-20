package com.flightontime.core.dto;

public record WeatherFeaturesDTO(
        double tempRangeF,      // Max Temp - Min Temp
        double dewpointRangeF,  // Max Dewpoint - Min Dewpoint
        int hasPrecip,          // 1 si hay lluvia, 0 si no
        int hasSnow,            // 1 si hay nieve, 0 si no
        int highWind            // 1 si viento >= 15 nudos
) {
}
