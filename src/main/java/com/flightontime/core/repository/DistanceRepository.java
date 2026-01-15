package com.flightontime.core.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@Repository
public class DistanceRepository {

    private static final String DISTANCE_FILE = "/distances.json";

    private final Map<String, Double> distanceByRoute;

    public DistanceRepository(ObjectMapper objectMapper) {
        this.distanceByRoute = loadDistances(objectMapper);
    }

    private Map<String, Double> loadDistances(ObjectMapper objectMapper) {
        try (InputStream is =
                     new ClassPathResource(DISTANCE_FILE).getInputStream()) {

            return objectMapper.readValue(
                    is,
                    new TypeReference<Map<String, Double>>() {}
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error loading distance map from JSON", e
            );
        }
    }

    public Optional<Double> findDistanceKm(String origin, String destination) {
        String key = origin + "-" + destination;
        return Optional.ofNullable(distanceByRoute.get(key));
    }
}
