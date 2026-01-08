package com.flightontime.core.service;

import ai.onnxruntime.OnnxTensor;
import com.flightontime.core.dto.PredictionResponseDTO;
import java.util.Map;

public interface FlightPredictionService {
    PredictionResponseDTO predecir(Map<String, OnnxTensor> features);
}
