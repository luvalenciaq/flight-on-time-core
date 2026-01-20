package com.flightontime.core.service.impl;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import com.flightontime.core.dto.WeatherFeaturesDTO;
import com.flightontime.core.model.Flight;
import com.flightontime.core.service.FeatureEngineeringService;
import com.flightontime.core.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeatureEngineeringServiceImpl implements FeatureEngineeringService {

    private final OrtEnvironment env;
    private final WeatherService weatherService;

    @Override
    public Map<String, OnnxTensor> transformar(Flight flight) throws Exception {
        Map<String, OnnxTensor> features = new HashMap<>();

        // 1. Obtener Features Climáticas (Llamada a la API externa)
        WeatherFeaturesDTO clima = weatherService.obtenerFeaturesClimaticas(flight.getOrigen(), flight.getFechaPartida());

        // 2. Calcular variables derivadas del Vuelo
        int hour = flight.getFechaPartida().getHour();
        int dayOfWeekNum = flight.getFechaPartida().getDayOfWeek().getValue() - 1; // Lunes=1 en Java -> Restar 1 para que Lunes=0 (Python)
        int isWeekend = (dayOfWeekNum >= 5) ? 1 : 0; // 5=Sabado, 6=Domingo
        String depTimeOfDay = getTimeOfDay(hour);

        // 3. Crear Tensores (Según contrato V2)
        // Categóricas (String) - Array de [1,1]
        features.put("OP_CARRIER", OnnxTensor.createTensor(env, new String[][]{{flight.getAerolinea()}}));
        features.put("ORIGIN", OnnxTensor.createTensor(env, new String[][]{{flight.getOrigen()}}));
        features.put("DEST", OnnxTensor.createTensor(env, new String[][]{{flight.getDestino()}}));
        features.put("dep_time_of_day", OnnxTensor.createTensor(env, new String[][]{{depTimeOfDay}}));

        // Numéricas (Int/Float) - Array de [1,1]
        // Nota: ONNX en Java es estricto con los tipos. Int -> long[][], Float -> float[][]

        // Inputs tipo 'int' en doc -> convertimos todo a (float)
        features.put("hour", OnnxTensor.createTensor(env, new float[][]{{hour}}));
        features.put("day_of_week_num", OnnxTensor.createTensor(env, new float[][]{{dayOfWeekNum}}));
        features.put("DISTANCE_KM", OnnxTensor.createTensor(env, new float[][]{{flight.getDistanciaKm().longValue()}}));

        // Inputs booleanos (0/1) -> Usamos long
        features.put("has_precip", OnnxTensor.createTensor(env, new float[][]{{clima.hasPrecip()}}));
        features.put("has_snow", OnnxTensor.createTensor(env, new float[][]{{clima.hasSnow()}}));
        features.put("high_wind", OnnxTensor.createTensor(env, new float[][]{{clima.highWind()}}));
        features.put("is_weekend", OnnxTensor.createTensor(env, new float[][]{{isWeekend}}));

        // Inputs tipo 'float' en doc -> Usamos float en Java
        features.put("temp_range_f", OnnxTensor.createTensor(env, new float[][]{{(float) clima.tempRangeF()}}));
        features.put("dewpoint_range_f", OnnxTensor.createTensor(env, new float[][]{{(float) clima.dewpointRangeF()}}));

        return features;
    }

    private String getTimeOfDay(int hour) {
        if (hour >= 5 && hour < 12) return "morning";
        if (hour >= 12 && hour < 18) return "afternoon";
        if (hour >= 18 && hour < 23) return "evening";
        return "night";
    }
}