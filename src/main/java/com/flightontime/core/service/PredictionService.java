package com.flightontime.core.service;

import com.flightontime.core.model.EstadoVuelo;
import com.flightontime.core.model.Flight;
import com.flightontime.core.model.PredictionResult;
import com.flightontime.core.util.DistanceCalculator;
import org.springframework.stereotype.Service;

//simulación
@Service
public class PredictionService {

    //aquí se va a cargar el archivo onnx a futuro
    public PredictionResult predict(Flight flight){

        // NUEVO: Calcular distancia automáticamente si no existe
        if (flight.getDistanciaKm() == null || flight.getDistanciaKm() == 0) {
            Double distancia = DistanceCalculator.calcularDistanciaRedondeada(
                    flight.getOrigen(),
                    flight.getDestino()
            );

            if (distancia != null) {
                flight.setDistanciaKm(distancia);
            } else {
                // Si no hay coordenadas, usar valor por defecto
                flight.setDistanciaKm(500.0);
            }
        }

        boolean isDelayed =
                flight.getDistanciaKm() > 300 &&
                        flight.getFechaPartida().getHour() >= 18;

        double probability = isDelayed ? 0.75 : 0.25;

        EstadoVuelo estado = isDelayed ? EstadoVuelo.RETRASADO : EstadoVuelo.PUNTUAL;

        PredictionResult result = new PredictionResult();
        result.setPrevision(estado);
        result.setProbabilidad(probability);
        result.setFlight(flight);

        return result;
    }
}
