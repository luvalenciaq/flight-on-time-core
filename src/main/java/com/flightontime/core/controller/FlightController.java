package com.flightontime.core.controller;

import com.flightontime.core.dto.FlightRequestDTO;
import com.flightontime.core.dto.PredictionResponseDTO;
import com.flightontime.core.model.Flight;
import com.flightontime.core.model.PredictionResult;
import com.flightontime.core.service.PredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/predict")
@RequiredArgsConstructor
public class FlightController {

    private final PredictionService predictionService;

    @PostMapping
    public PredictionResponseDTO predict(@Valid @RequestBody FlightRequestDTO dto) {

        Flight flight = new Flight(); //aqui convierto el dto a mi modelo de dominio -flight-
        flight.setAerolinea(dto.aerolinea());
        flight.setOrigen(dto.origen());
        flight.setDestino(dto.destino());
        flight.setFechaPartida(dto.fechaPartida());
        flight.setDistaciaKm(dto.distaciaKm());

        //hago la llamada a la lógica - servicio
        PredictionResult result = predictionService.predict(flight);

        //respuesta
        return new PredictionResponseDTO(
                result.getPrevision(),
                result.getProbabilidad()
        );
    }
}
