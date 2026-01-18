package com.flightontime.core.service;

import com.flightontime.core.exception.DistanceNotFoundException;
import com.flightontime.core.repository.DistanceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class DistanceService {

    private final DistanceRepository distanceRepository;

    public double getDistanceKm(String origenCodigo, String destinoCodigo) {
        return distanceRepository
                .findDistanciaKmByRoute(origenCodigo, destinoCodigo)
                .orElseThrow(() -> new DistanceNotFoundException(origenCodigo, destinoCodigo));
    }

    public boolean existeRutaDirecta(String origenCodigo, String destinoCodigo) {
        return distanceRepository
                .existsByOrigen_CodigoAndDestino_Codigo(origenCodigo, destinoCodigo);
    }
}

