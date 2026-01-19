package com.flightontime.core.util;

import com.flightontime.core.model.Airport;

/**
 * Utilidad para calcular distancias entre aeropuertos usando la fórmula de Haversine
 */
public class DistanceCalculator {

    private static final double RADIO_TIERRA_KM = 6371.0;

    // Constructor privado para evitar instanciación
    private DistanceCalculator() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no debe ser instanciada");
    }

    /**
     * Calcula la distancia en kilómetros entre dos puntos geográficos
     * usando la fórmula de Haversine
     *
     * @param lat1 Latitud del punto 1 en grados
     * @param lon1 Longitud del punto 1 en grados
     * @param lat2 Latitud del punto 2 en grados
     * @param lon2 Longitud del punto 2 en grados
     * @return Distancia en kilómetros
     */
    public static double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        // Convertir grados a radianes
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Diferencias
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        // Fórmula de Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_KM * c;
    }

    /**
     * Calcula la distancia entre dos aeropuertos
     *
     * @param origen Aeropuerto de origen
     * @param destino Aeropuerto de destino
     * @return Distancia en kilómetros, o null si alguno no tiene coordenadas
     */
    public static Double calcularDistancia(Airport origen, Airport destino) {
        if (origen == null || destino == null) {
            return null;
        }

        if (origen.getLatitud() == null || origen.getLongitud() == null ||
                destino.getLatitud() == null || destino.getLongitud() == null) {
            return null;
        }

        return calcularDistancia(
                origen.getLatitud().doubleValue(),
                origen.getLongitud().doubleValue(),
                destino.getLatitud().doubleValue(),
                destino.getLongitud().doubleValue()
        );
    }

    /**
     * Calcula la distancia entre dos aeropuertos y la redondea a 2 decimales
     *
     * @param origen Aeropuerto de origen
     * @param destino Aeropuerto de destino
     * @return Distancia en kilómetros redondeada, o null si alguno no tiene coordenadas
     */
    public static Double calcularDistanciaRedondeada(Airport origen, Airport destino) {
        Double distancia = calcularDistancia(origen, destino);
        if (distancia == null) {
            return null;
        }
        return Math.round(distancia * 100.0) / 100.0;
    }
}
