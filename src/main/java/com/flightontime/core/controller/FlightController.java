package com.flightontime.core.controller;

import ai.onnxruntime.OnnxTensor;
import com.flightontime.core.dto.FlightRequestDTO;
import com.flightontime.core.dto.PredictionResponseDTO;
import com.flightontime.core.exception.ModelInferenceException;
import com.flightontime.core.model.*;
import com.flightontime.core.repository.AirlineRepository;
import com.flightontime.core.repository.AirportRepository;
import com.flightontime.core.repository.FlightRepository;
import com.flightontime.core.repository.PredictionResultRepository;
import com.flightontime.core.service.FeatureEngineeringService;
import com.flightontime.core.service.FlightPredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Tag(name = "Flight Internal Prediction API", description = "API para predicciones de vuelos")
public class FlightController {

    private final FeatureEngineeringService featureService;
    private final FlightPredictionService predictionService;
    private final AirlineRepository airlineRepository;
    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final PredictionResultRepository predictionRepository;

    @Operation(summary = "Predecir puntualidad", description = "Recibe datos de un vuelo y devuelve la probabilidad de que sea puntual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Predicción exitosa", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PredictionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })

    @PostMapping("/predict")
    public ResponseEntity<?> predict(@Valid @RequestBody FlightRequestDTO dto) {

        try {
            Airline aerolinea = airlineRepository.findByCodigo(dto.aerolinea())
                    .orElseThrow(() -> new IllegalArgumentException("Aerolínea no encontrada: " + dto.aerolinea()));

            Airport origen = airportRepository.findByCodigo(dto.origen())
                    .orElseThrow(() -> new IllegalArgumentException("Aeropuerto origen no encontrado: " + dto.origen()));

            Airport destino = airportRepository.findByCodigo(dto.destino())
                    .orElseThrow(() -> new IllegalArgumentException("Aeropuerto destino no encontrado: " + dto.destino()));

            Flight flight = new Flight(); // aqui convierto el dto a mi modelo de dominio -flight-
            flight.setAerolinea(aerolinea);
            flight.setOrigen(origen);
            flight.setDestino(destino);
            flight.setFechaPartida(dto.fechaPartida());
            //borré el campo de distancia

            // 1. Transformar Flight a Map<String, OnnxTensor>
            Map<String, OnnxTensor> features = featureService.transformar(flight);

            // 2. Predecir usando el modelo ONNX
            PredictionResponseDTO resultado = predictionService.predecir(features);

            // 3. --- GUARDAR EN BASE DE DATOS ---
            // 3.1 Guardar el vuelo y obtener ID
            Flight savedFlight = flightRepository.save(flight);

            // 3.2 Crear el Prediction Result
            PredictionResult prediction = new PredictionResult();
            prediction.setFlight(savedFlight);
            prediction.setProbabilidad(resultado.probabilidad());
            prediction.setPrevision(EstadoVuelo.valueOf(resultado.prevision().toUpperCase()));

            // 3.3 Guardar la predicciòn
            predictionRepository.save(prediction);

            return ResponseEntity.ok(resultado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error datos: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error del modelo: " + e.getMessage());
        }
    }
}
