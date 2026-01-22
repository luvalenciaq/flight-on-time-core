package com.flightontime.core.controller;

import ai.onnxruntime.OnnxTensor;
import com.flightontime.core.dto.FlightRequestDTO;
import com.flightontime.core.dto.FlightResponseDTO;
import com.flightontime.core.dto.PredictionResponseDTO;
import com.flightontime.core.exception.FlightValidationException;
import com.flightontime.core.exception.ModelInferenceException;
import com.flightontime.core.model.*;
import com.flightontime.core.repository.AirlineRepository;
import com.flightontime.core.repository.AirportRepository;
import com.flightontime.core.repository.FlightRepository;
import com.flightontime.core.repository.PredictionResultRepository;
import com.flightontime.core.service.FeatureEngineeringService;
import com.flightontime.core.service.FlightPredictionService;
import com.flightontime.core.util.FlightValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Tag(name = "Flight Internal Prediction API", description = "API para predicciones de vuelos")
public class FlightController {

    private final FeatureEngineeringService featureService;
    private final FlightPredictionService predictionService;
    private final FlightValidator flightValidator;
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

        // 1. VALIDACIÓN Y CONSTRUCCIÓN
        // Si falla, FlightValidationException será capturada por GlobalExceptionHandler
        Flight flight = flightValidator.validarYConstruirVuelo(dto);

        // 2. Transformación de Features
        Map<String, OnnxTensor> features = featureService.transformar(flight);

        // 3. Predicción
        PredictionResponseDTO resultado = predictionService.predecir(features);

        // 4. Persistencia
        // Guardar el primer vuelo y obtener el ID
        Flight savedFlight = flightRepository.save(flight);

        // Crear la predicción
        PredictionResult prediction = new PredictionResult();
        prediction.setFlight(savedFlight);
        prediction.setProbabilidad(resultado.probabilidad());
        prediction.setPrevision(EstadoVuelo.valueOf(resultado.prevision().toUpperCase()));

        //Guardar la predicción
        predictionRepository.save(prediction);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/flights")
    @Operation(summary = "Listar vuelos", description = "Obtiene todos los vuelos guardados con sus predicciones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de vuelos obtenida exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FlightResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<FlightResponseDTO>> getAllFlights() {
        List<PredictionResult> predictions = predictionRepository.findAllWithFlightDetails();

        List<FlightResponseDTO> response = predictions.stream()
                .map(FlightResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }
}