package com.flightontime.core.service;

import com.flightontime.core.repository.DistanceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class DistanceService {

    private final DistanceRepository repository;

    public double getDistanceKm(String origin, String destination) {
        return repository.findDistanceKm(origin, destination)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Distance not found for route " + origin + "-" + destination
                        ));
    }
}

