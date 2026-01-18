package com.flightontime.core.service.impl;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import com.flightontime.core.model.Distance;
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

    @Override
    public Map<String, OnnxTensor> transformar(Flight flight, double distanciaKm) {

        try {
            // ---------------------------
            // 1. Features temporales
            // ---------------------------
            int hour = flight.getFechaPartida().getHour();
            int dayOfWeekNum = flight.getFechaPartida().getDayOfWeek().getValue() - 1; // 0=lunes
            boolean isWeekend = dayOfWeekNum == 5 || dayOfWeekNum == 6;
            String depTimeOfDay = calcularTimeOfDay(hour);

            // ---------------------------
            // 2. Clima (FASE 1: dummy)
            // ---------------------------
            float tempRangeF = 0.0f;
            float dewpointRangeF = 0.0f;
            int hasPrecip = 0;
            int hasSnow = 0;
            int highWind = 0;

            int distanciaKmModelo = (int) Math.round(distanciaKm);
            // ---------------------------
            // 3. Construcción del mapa
            // ---------------------------
            Map<String, OnnxTensor> features = new HashMap<>();

            features.put("OP_CARRIER",
                    OnnxTensor.createTensor(env,
                            new String[][]{{flight.getAerolinea().getCodigo()}}));

            features.put("ORIGIN",
                    OnnxTensor.createTensor(env,
                            new String[][]{{flight.getOrigen().getCodigo()}}));

            features.put("DEST",
                    OnnxTensor.createTensor(env,
                            new String[][]{{flight.getDestino().getCodigo()}}));

            features.put("hour",
                    OnnxTensor.createTensor(env,
                            new float[][]{{hour}}));

            features.put("day_of_week_num",
                    OnnxTensor.createTensor(env,
                            new float[][]{{dayOfWeekNum}}));

            features.put("dep_time_of_day",
                    OnnxTensor.createTensor(env,
                            new String[][]{{depTimeOfDay}}));

            features.put("DISTANCE_KM",
                    OnnxTensor.createTensor(env,
                            new float[][]{{distanciaKmModelo}}));

            features.put("temp_range_f",
                    OnnxTensor.createTensor(env,
                            new float[][]{{tempRangeF}}));

            features.put("dewpoint_range_f",
                    OnnxTensor.createTensor(env,
                            new float[][]{{dewpointRangeF}}));

            features.put("has_precip",
                    OnnxTensor.createTensor(env,
                            new float[][]{{hasPrecip}}));

            features.put("has_snow",
                    OnnxTensor.createTensor(env,
                            new float[][]{{hasSnow}}));

            features.put("high_wind",
                    OnnxTensor.createTensor(env,
                            new float[][]{{highWind}}));

            features.put("is_weekend",
                    OnnxTensor.createTensor(env,
                            new float[][]{{isWeekend ? 1 : 0}}));

            return features;

        } catch (OrtException e) {
            throw new IllegalStateException("Error creando tensores ONNX", e);
        }
    }

    private String calcularTimeOfDay(int hour) {
        if (hour >= 5 && hour <= 11) return "morning";
        if (hour >= 12 && hour <= 17) return "afternoon";
        if (hour >= 18 && hour <= 22) return "evening";
        return "night";
    }
}

