package com.flightontime.core.repository;

import com.flightontime.core.model.PredictionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PredictionResultRepository extends JpaRepository<PredictionResult, Long> {

}
