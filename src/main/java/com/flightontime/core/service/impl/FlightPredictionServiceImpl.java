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
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //Inicializa ONNX Runtime y carga el modelo al arrancar el servicio.
     //Se ejecuta una sola vez.
    @PostConstruct
    public void init() throws Exception {

        // Crear entorno ONNX
        env = OrtEnvironment.getEnvironment();

        // Cargar modelo desde resources como byte[]
        var is = getClass()
                .getClassLoader()
                .getResourceAsStream("modelo_prediccion_vuelos.onnx");

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

            // Output probabilities → FLOAT [-1, 2]
            float[][] probabilities =
                    (float[][]) result.get("probabilities").get().getValue();

            // Output label → INT64 [-1]
            long[] label =
                    (long[]) result.get("label").get().getValue();

            // Probabilidades de la primera fila (batch = 1)
            float probPuntual = probabilities[0][0];
            float probRetraso = probabilities[0][1];
            long predicted    = label[0];

            // Interpretación de la clase predicha
            String estado = (predicted == 1L)
                    ? "Retrasado"
                    : "Puntual";

            return new PredictionResponseDTO(
                    estado,
                    (double) probRetraso
            );

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