package com.flightontime.core.util;

import com.flightontime.core.dto.FlightRequestDTO;
import com.flightontime.core.exception.FlightValidationException;
import com.flightontime.core.model.Airline;
import com.flightontime.core.model.Airport;
import com.flightontime.core.model.Flight;
import com.flightontime.core.repository.AirlineRepository;
import com.flightontime.core.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FlightValidator {
    private final AirlineRepository airlineRepository;
    private final AirportRepository airportRepository;
    private final DistanceCalculator distanceCalculator;

    /**
     * Valida el DTO, verifica existencia en BD, calcula distancia
     * y devuelve el objeto Flight listo para ser procesado.
     */

    public Flight validarYConstruirVuelo(FlightRequestDTO dto) {

        Optional<Airline> airlineOpt = airlineRepository.findByCodigo(dto.aerolinea());
        Airline airline = airlineOpt.get();
        // 1. Validar Aerolínea
        if (!airlineRepository.existsByCodigo(airline.getCodigo())) {
            throw new FlightValidationException("La aerolínea '" + dto.aerolinea() + "' no existe o no está soportada.");
        }

        // 2. Validar que Origen y Destino sean diferentes
        if (dto.origen().equalsIgnoreCase(dto.destino())) {
            throw new FlightValidationException("El origen y el destino no pueden ser el mismo aeropuerto.");
        }

        // 3. Obtener Aeropuertos de la BD
        Optional<Airport> origenOpt = airportRepository.findByCodigo(dto.origen());
        Optional<Airport> destinoOpt = airportRepository.findByCodigo(dto.destino());

        if (origenOpt.isEmpty()) {
            throw new FlightValidationException("El aeropuerto de origen '" + dto.origen() + "' no es válido.");
        }
        if (destinoOpt.isEmpty()) {
            throw new FlightValidationException("El aeropuerto de destino '" + dto.destino() + "' no es válido.");
        }

        Airport origen = origenOpt.get();
        Airport destino = destinoOpt.get();

        // 4. Validar Coordenadas
        if (origen.getLatitud() == null || origen.getLongitud() == null) {
            throw new FlightValidationException("El aeropuerto de origen '" + dto.origen() + "' no tiene coordenadas registradas.");
        }
        if (destino.getLatitud() == null || destino.getLongitud() == null) {
            throw new FlightValidationException("El aeropuerto de destino '" + dto.destino() + "' no tiene coordenadas registradas.");
        }

        // 5. Calcular Distancia
        double distanciaKm = distanceCalculator.calcularDistancia(
                origen.getLatitud(), origen.getLongitud(),
                destino.getLatitud(), destino.getLongitud()
        );

        // 6. Construir Objeto de Dominio
        Flight flight = new Flight();
        flight.setAerolinea(airline);
        flight.setOrigen(origen);
        flight.setDestino(destino);
        flight.setFechaPartida(dto.fechaPartida());
        flight.setDistanciaKm(distanciaKm);

        return flight;
    }

}
