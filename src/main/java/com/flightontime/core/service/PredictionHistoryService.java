package com.flightontime.core.service;

import com.flightontime.core.dto.PredictionResponseDTO;
import com.flightontime.core.entity.PredictionLog;
import com.flightontime.core.model.Flight;
import com.flightontime.core.repository.PredictionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class PredictionHistoryService {
    @Autowired
    private PredictionLogRepository repository;

    /**
     * Guarda el historial de una predicción de forma asíncrona para no bloquear
     * la respuesta al usuario.
     */
    @Async // Opcional: Ejecuta el guardado en un hilo separado
    public void guardarHistorial(Flight request, PredictionResponseDTO response) {
        try {
            PredictionLog log = new PredictionLog();

            // Mapeamos los datos de entrada (Vuelo)
            log.setAerolinea(request.getAerolinea());
            log.setOrigen(request.getOrigen());
            log.setDestino(request.getDestino());
            // Asumiendo que fechaPartida viene como String, parsealo si es necesario.
            // Si en tu DTO ya es LocalDateTime, úsalo directo.
            log.setFechaPartida(request.getFechaPartida());
            log.setDistanciaKm(request.getDistanciaKm());

            // Mapeamos los datos de salida (Predicción)
            log.setResultadoPrediccion(response.prevision()); // Ajusta al nombre real de tu getter
            log.setProbabilidad(response.probabilidad());      // Ajusta al nombre real de tu getter

            // Guardamos en MySQL
            repository.save(log);

        } catch (Exception e) {
            // Es buena práctica loguear el error pero no romper el flujo principal
            System.err.println("Error al guardar historial: " + e.getMessage());
        }
    }
}
