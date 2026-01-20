package com.flightontime.core.controller;

import ai.onnxruntime.OnnxTensor;
import com.flightontime.core.dto.FlightRequestDTO;
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
    private final FlightValidator flightValidator;

    @Operation(summary = "Predecir puntualidad", description = "Recibe datos de un vuelo y devuelve la probabilidad de que sea puntual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Predicción exitosa", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PredictionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })

    @PostMapping("/predict")
    public ResponseEntity<?> predict(@Valid @RequestBody FlightRequestDTO dto) {

        try {
            // 1. VALIDACIÓN Y CONSTRUCCIÓN (Todo ocurre aquí dentro)
            // Si algo falla, lanza FlightValidationException y salta al catch
            Flight flight = flightValidator.validarYConstruirVuelo(dto);

            // 2. Transformación de Features
            Map<String, OnnxTensor> features = featureService.transformar(flight);

            // 3. Predicción
            PredictionResponseDTO resultado = predictionService.predecir(features);


            return ResponseEntity.ok(resultado);

        } catch (FlightValidationException e) {
            // Manejo de errores de validación (Aeropuerto no existe, etc.)
            return ResponseEntity.badRequest().body(Map.of(
                    "codigo", "VALIDATION_ERROR",
                    "mensaje", e.getMessage()
            ));
        } catch (Exception e) {
            // Manejo de errores técnicos inesperados
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "codigo", "INTERNAL_ERROR",
                    "mensaje", "Error procesando la predicción: " + e.getMessage()
            ));
        }
    }
}
