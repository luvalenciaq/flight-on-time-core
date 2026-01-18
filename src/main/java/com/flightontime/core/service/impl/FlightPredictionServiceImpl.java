package com.flightontime.core.service.impl;

import ai.onnxruntime.*;
import com.flightontime.core.dto.PredictionResponseDTO;
import com.flightontime.core.exception.ModelInferenceException;
import com.flightontime.core.service.FlightPredictionService;
import com.flightontime.core.web.exception.GlobalExceptionHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FlightPredictionServiceImpl implements FlightPredictionService {

    // Runtime ONNX (entorno + sesión del modelo)
    private OrtEnvironment env;
    private OrtSession session;
    private static final Logger log =
            LoggerFactory.getLogger(FlightPredictionServiceImpl.class);

    //Inicializa ONNX Runtime y carga el modelo al arrancar el servicio.
     //Se ejecuta una sola vez.
    @PostConstruct
    public void init() throws Exception {

        // Crear entorno ONNX
        env = OrtEnvironment.getEnvironment();

        // Cargar modelo desde resources como byte[]
        var is = getClass()
                .getClassLoader()
                .getResourceAsStream("v2_modelo_prediccion_vuelos.onnx");

        if (is == null) {
            throw new IllegalStateException("❌ No se encontró modelo_prediccion_vuelos.onnx en el classpath");
        }

        byte[] modelArray = is.readAllBytes();

        // Crear sesión de inferencia
        session = env.createSession(modelArray, new OrtSession.SessionOptions());

        log.info("✈️ Modelo ONNX cargado correctamente");

        // Log de inputs esperados por el modelo (debug)
        session.getInputInfo().forEach((name, info) ->
                log.info("Input ONNX → {} : {}", name, info.getInfo())
        );

        // Log de outputs del modelo (clave para interpretar resultados)
        session.getOutputInfo().forEach((name, info) ->
                log.info("Output ONNX → {} : {}", name, info.getInfo())
        );
    }

    //Ejecuta la inferencia del modelo ONNX a partir de los features preparados.
    public PredictionResponseDTO predecir(Map<String, OnnxTensor> features) {

        // Ejecutar inferencia usando inputs por nombre
        try (OrtSession.Result result = session.run(features)) {

            // Output del modelo: tensor INT64 [-1]
            // Contiene la clase predicha:
            // 0 = Puntual, 1 = Retrasado
            long[] labels =
                    (long[]) result.get("output_label").get().getValue();

            // Secuencia de mapas devuelta por ONNX
            @SuppressWarnings("unchecked")
            List<OnnxMap> probabilitiesSeq =
                    (List<OnnxMap>) result
                            .get("output_probability")
                            .get()
                            .getValue();

            // Validación básica por seguridad
            if (labels.length == 0 || probabilitiesSeq.isEmpty()) {
                throw new ModelInferenceException("Salida vacía del modelo");
            }

            // Como se procesa un solo vuelo (batch = 1),
            // se toma la primera posición
            long predictedLabel = labels[0];

            // Mapa de probabilidades del vuelo
            // key 0 → puntual
            // key 1 → retrasado
            Map<Long, Float> probMap =
                    (Map<Long, Float>) (Map<?, ?>) probabilitiesSeq.get(0).getValue();

            // Score de riesgo = probabilidad de retraso (clase 1)
            double scoreRiesgo = probMap.getOrDefault(1L, 0.0f);

            // Traducción del label a un estado legible
            String estado = (predictedLabel == 1L)
                    ? "Retrasado"
                    : "Puntual";

            // Respuesta final al cliente
            return new PredictionResponseDTO(estado, scoreRiesgo);

        } catch (OrtException e) {
            // Error durante la inferencia del modelo
            log.error("Error ejecutando inferencia ONNX", e);
            throw new ModelInferenceException("Fallo al ejecutar el modelo", e);

        } finally {
            // Liberar memoria nativa de los tensores de entrada
            features.values().forEach(tensor -> {
                try {
                    tensor.close();
                } catch (Exception ignored) {
                }
            });
        }
    }
}