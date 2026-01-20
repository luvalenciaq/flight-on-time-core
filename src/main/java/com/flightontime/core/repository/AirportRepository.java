package com.flightontime.core.repository;

import com.flightontime.core.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
    Optional<Airport> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
