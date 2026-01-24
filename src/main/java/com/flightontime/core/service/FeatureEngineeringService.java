package com.flightontime.core.service;

import com.flightontime.core.dto.TransformacionResultDTO;
import com.flightontime.core.dto.WeatherFeaturesDTO;
import com.flightontime.core.model.Flight;

public interface FeatureEngineeringService {
    TransformacionResultDTO transformar(Flight flight);
    WeatherFeaturesDTO getLastWeatherFeatures();
}
