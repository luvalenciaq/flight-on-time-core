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

public interface FlightPredictionService {
    PredictionResponseDTO predecir(Map<String, OnnxTensor> features);
}
