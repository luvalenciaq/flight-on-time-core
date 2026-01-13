package com.flightontime.core.repository;

import com.flightontime.core.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
    // Busqueda por codigo
    boolean existsByCodigo(String codigo);
}
