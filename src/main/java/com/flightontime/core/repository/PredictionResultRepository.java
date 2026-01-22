package com.flightontime.core.repository;

import com.flightontime.core.model.PredictionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PredictionResultRepository extends JpaRepository<PredictionResult, Long> {
    @Query("SELECT p FROM PredictionResult p " +
            "JOIN FETCH p.flight f " +
            "JOIN FETCH f.aerolinea " +
            "JOIN FETCH f.origen " +
            "JOIN FETCH f.destino")
    List<PredictionResult> findAllWithFlightDetails();
}
