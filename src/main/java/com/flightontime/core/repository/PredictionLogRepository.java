package com.flightontime.core.repository;

import com.flightontime.core.entity.PredictionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PredictionLogRepository extends JpaRepository<PredictionLog, Long> {
}
