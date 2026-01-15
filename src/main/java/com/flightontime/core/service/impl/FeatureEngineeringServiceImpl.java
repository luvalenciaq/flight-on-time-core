package com.flightontime.core.service.impl;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import com.flightontime.core.model.Flight;
import com.flightontime.core.service.DistanceService;
import com.flightontime.core.service.FeatureEngineeringService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FeatureEngineeringServiceImpl implements FeatureEngineeringService {

    // Entorno global de ONNX Runtime (se reutiliza en toda la app)
    private final OrtEnvironment env = OrtEnvironment.getEnvironment();
    private final DistanceService distanceService;

    public FeatureEngineeringServiceImpl(DistanceService distanceService) {
        this.distanceService = distanceService;
    }

    @Override
    public Map<String, OnnxTensor> transformar(Flight flight) {

        try {
            double distanciaKm = distanceService.getDistanceKm(
                    flight.getOrigen(),
                    flight.getDestino()
            );

            float distancia = (float) distanciaKm;
            // 1. Se obtienen los valores desde el dominio (Flight)
            // y se transforman al tipo esperado por el modelo ONNX.
            //-----float distancia = flight.getDistanciaKm().floatValue(); //aqui se espera float por eso se convierte
            //variables derivadas de la fecha
            int hora = flight.getFechaPartida().getHour();
            int diaSemana = flight.getFechaPartida().getDayOfWeek().getValue() - 1;
            int mes = flight.getFechaPartida().getMonthValue();

            //La clave del Map debe coincidir EXACTAMENTE
            // con el nombre de los inputs del modelo ONNX.
            Map<String, OnnxTensor> features = new HashMap<>();

            //El modelo espera tensores de shape [-1, 1]
            //por lo que se deben usar arreglos 2D: String[][] o float[][]
            features.put("aerolinea", OnnxTensor.createTensor(env, new String[][]{{flight.getAerolinea()}}));
            features.put("origen", OnnxTensor.createTensor(env, new String[][]{{flight.getOrigen()}}));
            features.put("destino", OnnxTensor.createTensor(env, new String[][]{{flight.getDestino()}}));
            features.put("distancia", OnnxTensor.createTensor(env, new float[][]{{distancia}}));
            features.put("hora", OnnxTensor.createTensor(env, new float[][]{{hora}}));
            features.put("dia_semana", OnnxTensor.createTensor(env, new float[][]{{diaSemana}}));
            features.put("mes", OnnxTensor.createTensor(env, new float[][]{{mes}}));
            /*
             * El mapa resultante se envía al servicio de inferencia,
             * donde será usado directamente en session.run(features).

             * El cierre de los tensores NO se hace aquí,
             * sino después de ejecutar la inferencia,
             * para evitar cerrar recursos antes de tiempo.
             */
            return features;

        } catch (OrtException e) {
            throw new IllegalStateException("Error creando tensores ONNX", e);
        }
    }
}

