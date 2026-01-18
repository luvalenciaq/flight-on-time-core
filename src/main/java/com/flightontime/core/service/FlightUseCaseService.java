package com.flightontime.core.service;

//orquestador, servicio que representa el caso de uso

import ai.onnxruntime.OnnxTensor;
import com.flightontime.core.dto.FlightRequestDTO;
import com.flightontime.core.dto.PredictionResponseDTO;
import com.flightontime.core.exception.AirlineNotFoundException;
import com.flightontime.core.exception.AirportNotFoundException;
import com.flightontime.core.model.*;
import com.flightontime.core.repository.AirlineRepository;
import com.flightontime.core.repository.AirportRepository;
import com.flightontime.core.repository.FlightRepository;
import com.flightontime.core.repository.PredictionResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FlightUseCaseService {

    private final AirlineRepository airlineRepository;
    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final PredictionResultRepository predictionRepository;
    private final DistanceService distanceService;
    private final FeatureEngineeringService featureService;
    private final FlightPredictionService predictionService;

    /**
     * Orquesta el flujo completo de predicción:
     * validaciones → inferencia → persistencia
     */
    public PredictionResponseDTO predictFlight(FlightRequestDTO dto) {

        Airline aerolinea = airlineRepository.findByCodigo(dto.aerolinea())
                .orElseThrow(() ->
                        new AirlineNotFoundException(dto.aerolinea()));

        Airport origen = airportRepository.findByCodigo(dto.origen())
                .orElseThrow(() ->
                        new AirportNotFoundException(dto.origen()));

        Airport destino = airportRepository.findByCodigo(dto.destino())
                .orElseThrow(() ->
                        new AirportNotFoundException(dto.destino()));

        // Construcción del agregado Flight (dominio)
        Flight flight = new Flight();
        flight.setAerolinea(aerolinea);
        flight.setOrigen(origen);
        flight.setDestino(destino);
        flight.setFechaPartida(dto.fechaPartida());

        // Distancia: dato fijo en BD, usado solo para inferencia
        double distanciaKm = distanceService.getDistanceKm(
                origen.getCodigo(),
                destino.getCodigo()
        );

        // Feature engineering → modelo ONNX
        Map<String, OnnxTensor> features =
                featureService.transformar(flight, distanciaKm);

        PredictionResponseDTO resultado =
                predictionService.predecir(features);

        // Persistencia
        Flight savedFlight = flightRepository.save(flight);

        PredictionResult prediction = new PredictionResult();
        prediction.setFlight(savedFlight);
        prediction.setProbabilidad(resultado.probabilidad());
        prediction.setPrevision(
                EstadoVuelo.valueOf(resultado.prevision().toUpperCase())
        );

        predictionRepository.save(prediction);

        return resultado;
    }
}

