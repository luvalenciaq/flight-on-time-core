package com.flightontime.core.controller;

import ai.onnxruntime.OnnxTensor;
import com.flightontime.core.dto.*;
import com.flightontime.core.model.*;
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

            // 1. VALIDACIÓN Y CONSTRUCCIÓN (Todo ocurre aquí dentro)
            // Si algo falla, lanza FlightValidationException y salta al catch
            Flight flight = flightValidator.validarYConstruirVuelo(dto);

            // 2. Transformación de Features
            TransformacionResultDTO transformacion = featureService.transformar(flight);
            Map<String, OnnxTensor> features = transformacion.features();

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

    @PostMapping("predict/detailed")
    public ResponseEntity<PredictionWithFeaturesDTO> predictDetailed(
            @Valid @RequestBody FlightRequestDTO dto
    ) {
        Flight flight = flightValidator.validarYConstruirVuelo(dto);
        TransformacionResultDTO transformacion = featureService.transformar(flight);
        Map<String, OnnxTensor> features = transformacion.features();
        WeatherFeaturesDTO weather = featureService.getLastWeatherFeatures();

        PredictionResponseDTO base =
                predictionService.predecir(transformacion.features());

        return ResponseEntity.ok(
                new PredictionWithFeaturesDTO(
                        base.prevision(),
                        base.probabilidad(),
                        transformacion.weatherFeatures()
                )
        );
    }
}