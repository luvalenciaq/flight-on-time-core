package com.flightontime.core.controller;

import com.flightontime.core.dto.FlightRequestDTO;
import com.flightontime.core.dto.PredictionResponseDTO;
import com.flightontime.core.model.Flight;
import com.flightontime.core.service.FeatureEngineeringService;
import com.flightontime.core.service.FlightPredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/predict")
@RequiredArgsConstructor
public class FlightController {

    private final FeatureEngineeringService featureService;
    private final FlightPredictionService predictionService;

    @PostMapping
    public ResponseEntity<?> predict(@Valid @RequestBody FlightRequestDTO dto) {

        Flight flight = new Flight(); // aqui convierto el dto a mi modelo de dominio -flight-
        flight.setAerolinea(dto.aerolinea());
        flight.setOrigen(dto.origen());
        flight.setDestino(dto.destino());
        flight.setFechaPartida(dto.fechaPartida());
        flight.setDistanciaKm(dto.distanciaKm());

        try {
            // 1. Transformar (modelo -> float[])
            float[] vector = featureService.transformar(flight);

            // 2. Predecir
            PredictionResponseDTO resultado = predictionService.predecir(vector);

            // 3. --- GUARDAR EN BASE DE DATOS ---
            // aqui ira la logica

            return ResponseEntity.ok(resultado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error datos: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error del modelo: " + e.getMessage());
        }
    }
}
