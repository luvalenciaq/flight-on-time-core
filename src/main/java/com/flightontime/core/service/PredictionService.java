package com.flightontime.core.service;

import com.flightontime.core.model.Flight;
import com.flightontime.core.model.PredictionResult;
import org.springframework.stereotype.Service;

//simulación
@Service
public class PredictionService {

    //aquí se va a cargar el archivo onnx a futuro
    public PredictionResult predict(Flight flight){

        boolean isDelayed =
                flight.getDistaciaKm() > 300 &&
                        flight.getFechaPartida().getHour() >= 18;

        double probability = isDelayed ? 0.75 : 0.25;

        String status = isDelayed ? "Retrasado" : "Puntual";

        return new PredictionResult(status, probability);
    }
}
