package com.flightontime.core.service.impl;

import ai.onnxruntime.*;
import com.flightontime.core.dto.PredictionResponseDTO;
import com.flightontime.core.exception.ModelInferenceException;
import com.flightontime.core.service.FlightPredictionService;
import com.flightontime.core.web.exception.GlobalExceptionHandler;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FlightPredictionServiceImpl implements FlightPredictionService {

    private OrtEnvironment env;
    private OrtSession session;
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @PostConstruct
    public void init() throws Exception {
        env = OrtEnvironment.getEnvironment();
        // Carga del modelo desde resources
        byte[] modelArray = getClass().getResourceAsStream("/vuelosclima_rfmodelo.onnx").readAllBytes();
        session = env.createSession(modelArray, new OrtSession.SessionOptions());

        log.info("✈️ Modelo ONNX cargado correctamente");
    }

    @Override
    public PredictionResponseDTO predecir(Map<String, OnnxTensor> features) {
        try (OrtSession.Result result = session.run(features)) {

            // 1. Obtener LABEL (Clase predicha) -> long[]
            long[] label = (long[]) result.get("output_label").get().getValue();
            long predicted = label[0];

            // 2. Obtener PROBABILIDAD (Corregido para desempaquetar OnnxMap)
            // El output es una Secuencia (Lista)
            OnnxSequence sequence = (OnnxSequence) result.get("output_probability").get();

            // Usamos el tipo genérico correcto (? extends OnnxValue)
            List<? extends OnnxValue> mapList = sequence.getValue();

            // Obtenemos el primer OnnxMap (del primer elemento del batch)
            OnnxMap onnxMap = (OnnxMap) mapList.get(0);

            // Ahora sí, extraemos el Map de Java del OnnxMap
            @SuppressWarnings("unchecked")
            Map<Long, Float> mapProb = (Map<Long, Float>) onnxMap.getValue();

            // Buscamos la probabilidad de la clase 1 (Retraso)
            // Usamos 1L porque ONNX guarda las claves como Long
            float probRetraso = mapProb.getOrDefault(1L, 0.0f);

            // 3. Resultado
            String estado = (predicted == 1L) ? "Retrasado" : "Puntual";

            return new PredictionResponseDTO(estado, (double) probRetraso);

        } catch (OrtException e) {
            log.error("Error ejecutando inferencia ONNX", e);
            throw new ModelInferenceException("Fallo al ejecutar el modelo: " + e.getMessage(), e);
        } finally {
            // Liberar memoria de los tensores de entrada para evitar fugas
            if (features != null) {
                features.values().forEach(t -> {
                    try { t.close(); } catch (Exception e) {}
                });
            }
        }
    }
}