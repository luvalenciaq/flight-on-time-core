package com.flightontime.core.dto;

import ai.onnxruntime.OnnxTensor;

import java.util.Map;

public record TransformacionResultDTO( //para agregar el resultado de la tranformación en un objeto
        Map<String, OnnxTensor> features,
        WeatherFeaturesDTO weatherFeatures
) {}
