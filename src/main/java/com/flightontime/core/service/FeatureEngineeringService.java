package com.flightontime.core.service;

import ai.onnxruntime.OnnxTensor;
import com.flightontime.core.model.Flight;

import java.util.Map;

public interface FeatureEngineeringService {
    Map<String, OnnxTensor> transformar(Flight flight) throws Exception;}
