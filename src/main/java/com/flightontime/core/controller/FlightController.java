package com.flightontime.core.controller;

import com.flightontime.core.dto.FlightRequestDTO;
import com.flightontime.core.dto.PredictionResponseDTO;
import com.flightontime.core.service.FlightUseCaseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Tag(name = "Flight Internal Prediction API")
public class FlightController {

    private final FlightUseCaseService flightUseCaseService;

    @PostMapping("/predict")
    public ResponseEntity<PredictionResponseDTO> predict(
            @Valid @RequestBody FlightRequestDTO dto
    ) {
        return ResponseEntity.ok(
                flightUseCaseService.predictFlight(dto)
        );
    }
}

