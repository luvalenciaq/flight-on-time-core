package com.flightontime.core.controller;

import com.flightontime.core.dto.FlightRequestDTO;
import com.flightontime.core.dto.PredictionResponseDTO;
import com.flightontime.core.model.Flight;
import com.flightontime.core.repository.AirlineRepository;
import com.flightontime.core.repository.AirportRepository;
import com.flightontime.core.service.FeatureEngineeringService;
import com.flightontime.core.service.FlightPredictionService;
import com.flightontime.core.service.PredictionHistoryService;
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
import java.util.Map;
import ai.onnxruntime.OnnxTensor;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Tag(name = "Flight Internal Prediction API", description = "API para predicciones de vuelos")
public class FlightController {

    private final FeatureEngineeringService featureService;
    private final FlightPredictionService predictionService;
    private final PredictionHistoryService historyService;
    private final AirlineRepository airlineRepository;
    private final AirportRepository airportRepository;

    @Operation(summary = "Predecir puntualidad", description = "Recibe datos de un vuelo y devuelve la probabilidad de que sea puntual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Predicción exitosa", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PredictionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })

    @PostMapping("/predict")
    public ResponseEntity<?> predict(@Valid @RequestBody FlightRequestDTO dto) {
        // VALIDACIONES CONTRA BASE DE DATOS

        // 1. Validar Aerolínea
        if (!airlineRepository.existsByCodigo(dto.aerolinea())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La aerolínea '" + dto.aerolinea() + "' no existe o no está soportada."));
        }

        // 2. Validar Origen
        if (!airportRepository.existsByCodigo(dto.origen())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El aeropuerto de origen '" + dto.origen() + "' no es válido."));
        }

        // 3. Validar Destino
        if (!airportRepository.existsByCodigo(dto.destino())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El aeropuerto de destino '" + dto.destino() + "' no es válido."));
        }

        // 4. Validar que Origen y Destino no sean iguales
        if (dto.origen().equals(dto.destino())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El origen y el destino no pueden ser el mismo aeropuerto."));
        }

        // FIN VALIDACIONES

        Flight flight = new Flight(); // aqui convierto el dto a mi modelo de dominio -flight-
        flight.setAerolinea(dto.aerolinea());
        flight.setOrigen(dto.origen());
        flight.setDestino(dto.destino());
        flight.setFechaPartida(dto.fechaPartida());
        flight.setDistanciaKm(dto.distanciaKm());

        try {
            // 1. Transformar (modelo -> float[])
            Map<String, OnnxTensor> features = featureService.transformar(flight);

            // 2. Predecir
            PredictionResponseDTO resultado = predictionService.predecir(features);

            // 3. --- GUARDAR EN BASE DE DATOS ---
            historyService.guardarHistorial(flight, resultado);

            return ResponseEntity.ok(resultado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error datos: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error del modelo: " + e.getMessage());
        }
    }
}
