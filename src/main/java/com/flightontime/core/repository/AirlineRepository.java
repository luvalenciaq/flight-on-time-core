package com.flightontime.core.repository;

import com.flightontime.core.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, Long> {
    //búsqueda por código
    boolean existsByCodigo(String codigo);
}
