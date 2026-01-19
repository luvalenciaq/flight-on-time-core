package com.flightontime.core.util;

import org.springframework.stereotype.Component;

@Component
public class DistanceCalculator {

    private static final int RADIO_TIERRA_KM = 6371;

    public double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        // Fórmula de Haversine
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_KM * c;
    }

    public double calcularDistanciaEnMillas(double lat1, double lon1, double lat2, double lon2) {
        return calcularDistancia(lat1, lon1, lat2, lon2) * 0.621371;
    }
}