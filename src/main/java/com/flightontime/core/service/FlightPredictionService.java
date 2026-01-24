package com.flightontime.core.service;

import ai.onnxruntime.OnnxTensor;
import com.flightontime.core.dto.PredictionResponseDTO;
import com.flightontime.core.dto.PredictionWithFeaturesDTO;
import com.flightontime.core.dto.WeatherFeaturesDTO;

import java.util.Map;

public interface FlightPredictionService {
    PredictionResponseDTO predecir(Map<String, OnnxTensor> features);

    PredictionWithFeaturesDTO predecirConFeatures(
            Map<String, OnnxTensor> features,
            WeatherFeaturesDTO weatherFeatures
    );
}
