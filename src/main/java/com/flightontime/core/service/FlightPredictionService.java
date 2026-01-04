package com.flightontime.core.service;

import ai.onnxruntime.*;
import com.flightontime.core.dto.PredictionResponseDTO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
/**
 * Servicio de Predicción de Vuelos (Motor de Inferencia).
 * <p>
 * Esta clase es responsable de cargar el modelo de Inteligencia Artificial
 * (ONNX)
 * y ejecutar las predicciones sobre los datos ya procesados.
 * Gestiona el ciclo de vida del entorno ONNX (carga al inicio, cierre al
 * final).
 * </p>
 */
public class FlightPredictionService {
    private OrtEnvironment env;
    private OrtSession session;

    /**
     * Inicialización del Servicio.
     * <p>
     * Se ejecuta una sola vez al arrancar la aplicación (@PostConstruct).
     * Carga el archivo del modelo (.onnx) desde los recursos en memoria
     * para que las predicciones sean rápidas.
     * </p>
     */
    @PostConstruct
    public void init() throws OrtException, IOException {
        this.env = OrtEnvironment.getEnvironment();
        // Carga el modelo "modelo_vuelos.onnx" desde src/main/resources
        byte[] modelArray = getClass().getResourceAsStream("/modelo_vuelos.onnx").readAllBytes();
        this.session = env.createSession(modelArray, new OrtSession.SessionOptions());
        System.out.println("✈️ Modelo ONNX cargado exitosamente en memoria");
    }

    /**
     * Ejecuta la predicción para un vuelo específico.
     *
     * @param inputData Vector de características numéricas (float[]) proveniente
     *                  del FeatureEngineeringService.
     * @return ResultadoPrevisionDTO Objeto con la clasificación (Puntual/Retraso) y
     *         la probabilidad.
     * @throws OrtException Si ocurre un error interno en el motor de ONNX (ej.
     *                      tipos incompatibles).
     */
    public PredictionResponseDTO predecir(float[] inputData) throws OrtException {
        // 1. Crear Tensor de Entrada
        // ONNX espera una matriz, incluso para una sola fila.
        // Dimensiones: [1 fila, N columnas] -> [1, 12]
        long[] shape = new long[] { 1, inputData.length };
        OnnxTensor tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData), shape);

        // 2. Ejecutar la Inferencia
        // Pasamos un mapa con las entradas. La clave "input" debe coincidir con el
        // nombre
        // del nodo de entrada definido en el modelo cuando se entrenó.
        try (OrtSession.Result result = session.run(Collections.singletonMap("input", tensor))) {

            // 3. Extracción de Resultados (Desempaquetado Complejo)
            // El modelo devuelve dos cosas: etiquetas (0/1) y probabilidades.
            // Accedemos al índice 1 del resultado, que contiene la secuencia de mapas de
            // probabilidades.

            // Paso A: Obtener la lista cruda de resultados (uno por fila del batch)
            List<?> outputList = (List<?>) result.get(1).getValue();

            // Paso B: Obtener el resultado para nuestro único vuelo (índice 0)
            // Esto nos da un objeto intermedio 'OnnxMap'.
            OnnxMap mapContainer = (OnnxMap) outputList.get(0);

            // Paso C: Extraer el Map real de Java desde el contenedor ONNX
            // Clave (Long): Clase (0=Puntual, 1=Retraso)
            // Valor (Float): Probabilidad (0.0 a 1.0)
            @SuppressWarnings("unchecked")
            Map<Long, Float> probs = (Map<Long, Float>) mapContainer.getValue();

            // 4. Interpretación del Negocio
            // Buscamos la probabilidad de la clase 1 (Retraso). Default 0.0 si no existe.
            float probRetraso = probs.getOrDefault(1L, 0.0f);

            // Umbral de decisión: 0.5 (50%)
            String estado = (probRetraso >= 0.4) ? "Alto Riesgo de Retraso"
              : (probRetraso >= 0.3) ? "Riesgo Moderado"
                    : (probRetraso >= 0.2) ? "Riesgo Bajo"
                    : "Puntual";

            return new PredictionResponseDTO(estado, probRetraso);
        }
    }

    /**
     * Limpieza de Recursos.
     * <p>
     * Se ejecuta al detener la aplicación (@PreDestroy).
     * Es crucial cerrar la sesión y el entorno para liberar la memoria nativa (C++)
     * que utiliza ONNX Runtime.
     * </p>
     */
    @PreDestroy
    public void cleanup() throws OrtException {
        if (session != null)
            session.close();
        if (env != null)
            env.close();
    }
}
