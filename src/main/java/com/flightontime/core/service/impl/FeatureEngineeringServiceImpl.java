package com.flightontime.core.service.impl;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import com.flightontime.core.dto.TransformacionResultDTO;
import com.flightontime.core.dto.WeatherFeaturesDTO;
import com.flightontime.core.model.Flight;
import com.flightontime.core.service.FeatureEngineeringService;
import com.flightontime.core.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeatureEngineeringServiceImpl implements FeatureEngineeringService {

    // Entorno global de ONNX Runtime (se reutiliza en toda la app)
    private final OrtEnvironment env = OrtEnvironment.getEnvironment();
    private final WeatherService weatherService;
    // Variable para guardar las últimas features del clima utilizadas
    private WeatherFeaturesDTO lastWeatherFeatures;

    @Override
    public TransformacionResultDTO transformar(Flight flight) {

        try {
            // 1. Se obtienen los valores desde el dominio (Flight)
            // y se transforman al tipo esperado por el modelo ONNX.
            float distancia = flight.getDistanciaKm().floatValue(); //aqui se espera float por eso se convierte
            //variables derivadas de la fecha
            int hora = flight.getFechaPartida().getHour();
            int diaSemana = flight.getFechaPartida().getDayOfWeek().getValue() - 1;
            LocalDateTime fecha = flight.getFechaPartida();

            //La clave del Map debe coincidir EXACTAMENTE
            // con el nombre de los inputs del modelo ONNX.
            Map<String, OnnxTensor> features = new HashMap<>();

            //El modelo espera tensores de shape [-1, 1]
            //por lo que se deben usar arreglos 2D: String[][] o float[][]
            features.put("OP_CARRIER", OnnxTensor.createTensor(env, new String[][]{{flight.getAerolinea().getCodigo()}}));
            features.put("ORIGIN", OnnxTensor.createTensor(env, new String[][]{{flight.getOrigen().getCodigo()}}));
            features.put("DEST", OnnxTensor.createTensor(env, new String[][]{{flight.getDestino().getCodigo()}}));
            features.put("DISTANCE_KM", OnnxTensor.createTensor(env, new float[][]{{distancia}}));
            features.put("hour", OnnxTensor.createTensor(env, new float[][]{{hora}}));
            features.put("day_of_week_num", OnnxTensor.createTensor(env, new float[][]{{diaSemana}}));
            features.put("dep_time_of_day", OnnxTensor.createTensor(env, new String[][]{{getTimeOfDay(fecha)}}));
            // VARIABLES CLIMÁTICAS - DATOS REALES DE LA API
            WeatherFeaturesDTO weather = weatherService.obtenerFeaturesClimaticas(flight.getOrigen().getCodigo(), fecha);

            // GUARDAR las features del clima para usarlas después en la respuesta
            this.lastWeatherFeatures = weather;

            features.put("temp_range_f",
                    OnnxTensor.createTensor(env, new float[][]{{(float) weather.tempRangeF()}}));
            features.put("dewpoint_range_f",
                    OnnxTensor.createTensor(env, new float[][]{{(float)weather.dewpointRangeF()}}));
            features.put("has_precip",
                    OnnxTensor.createTensor(env, new float[][]{{weather.hasPrecip()}}));
            features.put("has_snow",
                    OnnxTensor.createTensor(env, new float[][]{{weather.hasSnow()}}));
            features.put("high_wind",
                    OnnxTensor.createTensor(env, new float[][]{{weather.highWind()}}));


            // is_weekend: 1 si es sábado (6) o domingo (7), 0 en caso contrario
            int dayOfWeek = fecha.getDayOfWeek().getValue(); // 1=lunes, 7=domingo
            float isWeekend = (dayOfWeek == 6 || dayOfWeek == 7) ? 1.0f : 0.0f;
            features.put("is_weekend",
                    OnnxTensor.createTensor(env, new float[][]{{isWeekend}}));
            /*
             * El mapa resultante se envía al servicio de inferencia,
             * donde será usado directamente en session.run(features).

             * El cierre de los tensores NO se hace aquí,
             * sino después de ejecutar la inferencia,
             * para evitar cerrar recursos antes de tiempo.
             */
            return new TransformacionResultDTO(features, weather);

        } catch (OrtException e) {
            throw new IllegalStateException("Error creando tensores ONNX", e);
        }
    }

    @Override
    public WeatherFeaturesDTO getLastWeatherFeatures() {
        return this.lastWeatherFeatures;
    }
    /**
     * Determina la franja horaria de salida según la documentación:
     * - morning: 05:00–11:59
     * - afternoon: 12:00–17:59
     * - evening: 18:00–22:59
     * - night: resto de horas (23:00–04:59)
     */
    private String getTimeOfDay(LocalDateTime dateTime) {
        int hour = dateTime.getHour();

        if (hour >= 5 && hour < 12) {
            return "morning";
        } else if (hour >= 12 && hour < 18) {
            return "afternoon";
        } else if (hour >= 18 && hour < 23) {
            return "evening";
        } else {
            return "night";
        }
    }
}
