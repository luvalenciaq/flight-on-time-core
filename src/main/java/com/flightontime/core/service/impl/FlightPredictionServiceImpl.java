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
                .getResourceAsStream("vuelosclima_rfmodelo.onnx");

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

        try (OrtSession.Result result = session.run(features)) {
            java.util.Map<Long, Float> probabilidades = extractProbabilityMap(result);

            // 1. Obtener la predicción (0 o 1)
            double probPuntual = probabilidades.get(0L).doubleValue();
            double probRetrasado = probabilidades.get(1L).doubleValue();

            // 2. Extraer probabilidades del modelo
            double scoreRiesgo = probRetrasado;

            // 3. Convertir a texto
            String estado = (scoreRiesgo >= 0.5) ? "RETRASADO" : "PUNTUAL";

            log.info("🎯 Predicción: {} con probabilidad: {}", estado, scoreRiesgo);

            return new PredictionResponseDTO(estado, scoreRiesgo);

        } catch (OrtException e) {
            log.error("❌ Error ejecutando inferencia ONNX", e);
            throw new ModelInferenceException("Fallo al ejecutar el modelo", e);
        } finally {
            features.values().forEach(tensor -> {
                try {
                    tensor.close();
                } catch (Exception ignored) {
                }
            });
        }
    }

    private java.util.Map<Long, Float> extractProbabilityMap(OrtSession.Result result) throws OrtException {
        OnnxValue probValue = result.get("output_probability").get();
        OnnxSequence sequence = (OnnxSequence) probValue;
        java.util.List<?> list = (java.util.List<?>) sequence.getValue();
        OnnxMap onnxMap = (OnnxMap) list.get(0);

        @SuppressWarnings("unchecked")
        java.util.Map<Long, Float> probMap = (java.util.Map<Long, Float>) onnxMap.getValue();

        return probMap;
    }

    //Libera los recursos de ONNX Runtime cuando la aplicación se cierra.
    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) session.close();
            if (env != null) env.close();
            log.info("✅ Recursos ONNX liberados correctamente");
        } catch (Exception e) {
            log.error("Error al liberar recursos ONNX", e);
        }
    }
}